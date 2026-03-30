package jp.go.meti.drone.com.common.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

import jp.go.meti.drone.com.common.logging.ApplicationLogger;
import jp.go.meti.drone.com.common.logging.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * DB共通カラムを更新するインターセプタークラス。
 * <p>
 * MyBatisがExecutorインスタンスのupdateメソッドを実行する際に、<br>
 * 本クラスのinterceptメソッドで定義した処理を割り込ませる。
 * </p>
 * 
 * @version $Revision$
 */
@Component
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
@Slf4j
public class CommonPropertyUpdateInterceptor implements Interceptor {

    /**
     * アプリケーションロガー。
     */
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);

    /**
     * モデルオブジェクトの作成日時フィールド名。
     */
    private static final String CREATE_DATETIME = "createDatetime";

    /**
     * モデルオブジェクトの更新日時フィールド名。
     */
    private static final String UPDATE_DATETIME = "updateDatetime";

    /**
     * モデルオブジェクトの論理削除フラグフィールド名。
     */
    private static final String LOGIC_DELETE_FLAG = "logicDeleteFlag";

    /**
     * interceptメソッド。
     * <p>
     * {@link Interceptor#intercept}メソッドを、以下の処理でオーバーライドする。<br>
     * {@link Executor#update}メソッドを呼び出す際に渡される引数を解析する。<br>
     * 第一引数の{@link MappedStatement}からインターセプト要否を判断し、<br>
     * 第二引数のモデルオブジェクトに対して割り込み処理を実行する。
     * </p>
     * 
     * @param invocation {@link Invocation}
     * @return SQL実行メソッドの戻り値({@link Invocation#proceed})
     * @throws Throwable インターセプト処理で発生し得る例外({@link IllegalAccessException}、{@link InvocationTargetException})
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        // Executor.update()メソッドの引数を取得
        // 第一引数： MappedStatement
        // 第二引数： モデルオブジェクト
        Object[] args = invocation.getArgs();

        // 引数オブジェクト、第一引数、第二引数のNullチェック
        if (Objects.isNull(args)) {
            appLogger.info(CommonPropertyInterceptorMessageKeys.I_COMMON_0001.getKey(), "Invocation#getArgs");
            return invocation.proceed();
        } else if (Objects.isNull(args[0])) {
            appLogger.info(CommonPropertyInterceptorMessageKeys.I_COMMON_0001.getKey(), "MappedStatement");
            return invocation.proceed();
        } else if (Objects.isNull(args[1])) {
            appLogger.info(CommonPropertyInterceptorMessageKeys.I_COMMON_0001.getKey(), "Object");
            return invocation.proceed();
        }

        // SQLコマンドタイプの初期化
        SqlCommandType sqlCommandType = null;

        // 第一引数からSQLコマンドタイプを判定
        MappedStatement mappedStatement = (MappedStatement) args[0];
        sqlCommandType = mappedStatement.getSqlCommandType();

        // SQLコマンドタイプがINSERT、またはUPDATEの場合のみ、割り込み処理を実行
        if (sqlCommandType == SqlCommandType.INSERT || sqlCommandType == SqlCommandType.UPDATE) {

            // 第二引数に対する割り込み処理を実行
            // 第二引数には、リポジトリのメソッドに渡したモデルオブジェクトが格納される。
            // ただし、リポジトリの各メソッドシグニチャにより、モデルオブジェクトがそのまま渡されてくる場合と、
            // Mapオブジェクトにラッピングされて渡されてくる場合がある。
            this.setCommonProperty(args[1], sqlCommandType);
        }

        // SQLを実行し、SQL実行結果を返却
        return invocation.proceed();
    }

    /**
     * pluginメソッド。
     * <p>
     * {@link Interceptor#plugin}メソッドを形式的にオーバーライドする。
     * </p>
     * 
     * @param target {@link Object}
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * setPropertiesメソッド。
     * <p>
     * {@link Interceptor#setProperties}メソッドを形式的にオーバーライドする。
     * </p>
     * 
     * @param properties {@link Properties}
     */
    @Override
    public void setProperties(Properties properties) {
    	// This method is intentionally left blank
    }

    /**
     * モデルオブジェクトのDB共通カラムを更新するメソッド。
     * <p>
     * SQLコマンドタイプに応じて、以下の項目をセットする。<br>
     * INSERTの場合： モデルオブジェクトの作成日時、更新日時、論理削除フラグをセットする。<br>
     * UPDATEの場合： モデルオブジェクトの更新日時をセットする。
     * </p>
     *
     * @param model モデルオブジェクト
     * @param sqlCommandType SQLコマンドタイプ({@link SqlCommandType})
     * @throws IllegalAccessException {@link BeanUtils#setProperty}メソッドからスローされる。
     * @throws InvocationTargetException {@link BeanUtils#setProperty}メソッドからスローされる。
     */
    private void setCommonProperty(Object model, SqlCommandType sqlCommandType) throws IllegalAccessException,
        InvocationTargetException {
        // 現在日時の取得
        LocalDateTime localDateTimeNow = LocalDateTime.now();

        // 論理削除フラグの設定
        boolean isNotDeleted = false;

        if (sqlCommandType == SqlCommandType.INSERT) {
            // MyBatisGeneratorで自動生成したリポジトリのinsertメソッドと
            // insertSelectiveメソッドの場合は、モデルオブジェクトが渡されてくる。
            BeanUtils.setProperty(model, CREATE_DATETIME, localDateTimeNow);
            BeanUtils.setProperty(model, UPDATE_DATETIME, localDateTimeNow);
            BeanUtils.setProperty(model, LOGIC_DELETE_FLAG, isNotDeleted);
        } else if (sqlCommandType == SqlCommandType.UPDATE) {
            if (model instanceof Map) {
                // MyBatisGeneratorで自動生成したリポジトリのupdateByExampleメソッドと
                // updateByExampleSelectiveメソッドの場合は、モデルオブジェクトがMapオブジェクトに格納される。
                // メソッドの引数が2つ以上の場合や、引数に@Paramアノテーションを付与した場合にこの挙動となる。
                // この場合、モデルオブジェクトのフィールド名だけでは値を更新できず、
                // "MapのKey項目名.フィールド名"で指定する必要がある。
                Map<Object, Object> map = (Map<Object, Object>) model;
                for (Map.Entry<Object, Object> obj : map.entrySet()) {
                    String updateDatetime = obj.getKey().toString() + "." + UPDATE_DATETIME;
                    BeanUtils.setProperty(model, updateDatetime, localDateTimeNow);
                }
            } else {
                // MyBatisGeneratorで自動生成したリポジトリのupdateByPrimaryKeyメソッドと
                // updateByPrimaryKeySelectiveメソッドの場合は、モデルオブジェクトが渡されてくる。
                BeanUtils.setProperty(model, UPDATE_DATETIME, localDateTimeNow);
            }
        }
    }
}
