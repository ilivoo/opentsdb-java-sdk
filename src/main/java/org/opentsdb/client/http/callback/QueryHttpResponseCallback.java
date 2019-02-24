package org.opentsdb.client.http.callback;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.opentsdb.client.bean.request.Query;
import org.opentsdb.client.bean.response.QueryResult;
import org.opentsdb.client.common.Json;
import org.opentsdb.client.exception.http.HttpException;
import org.opentsdb.client.util.ResponseUtil;

import java.io.IOException;
import java.util.List;

/**
 * 异步查询回调
 *
 * @Author: jinyao
 * @Description:
 * @CreateDate: 2019/2/24 下午4:14
 * @Version: 1.0
 */
@Slf4j
public class QueryHttpResponseCallback implements FutureCallback<HttpResponse> {

    private final QueryCallback callback;

    private final Query query;

    public QueryHttpResponseCallback(QueryCallback callback, Query query) {
        this.callback = callback;
        this.query = query;
    }

    @Override
    public void completed(HttpResponse response) {
        try {
            List<QueryResult> results = Json.readValue(ResponseUtil.getContent(response), List.class, QueryResult.class);
            log.debug("请求成功");
            this.callback.response(query, results);
        } catch (IOException | HttpException e) {
            e.printStackTrace();
            log.error("请求失败，query:{},error:{}", query, e.getMessage());
            this.callback.failed(query, e);
        }
    }

    @Override
    public void failed(Exception e) {
        log.error("请求失败，query:{},error:{}", query, e.getMessage());
        this.callback.failed(query, e);
    }

    @Override
    public void cancelled() {

    }

    /***
     * 定义查询callback，需要用户自己实现逻辑
     */
    public interface QueryCallback {

        /***
         * 在请求完成并且response code成功时回调
         * @param query
         * @param queryResults
         */
        void response(Query query, List<QueryResult> queryResults);

        /***
         * 在发生错误是回调，如果http成功complete，但response code大于400，也会调用这个方法
         * @param query
         * @param e
         */
        void failed(Query query, Exception e);

    }

}
