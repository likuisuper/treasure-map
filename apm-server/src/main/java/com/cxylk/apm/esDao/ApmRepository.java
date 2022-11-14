package com.cxylk.apm.esDao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @author likui
 * @date 2022/11/11 下午1:54
 **/
public interface ApmRepository extends ElasticsearchRepository<TraceNode,String> {
    /**
     * 根据traceId搜索，By后面的字符串必须存在TreeNode中，可以使用and连接
     * @param traceId
     * @return
     */
    List<TraceNode> findByTraceId(String traceId);

    List<TraceNode> findByModeType(String modeType);

    List<TraceNode> findByModeType(String modeType, Pageable pageable);

    List<TraceNode> findByTraceIdAndSpanId(String traceId, String spanId);
}
