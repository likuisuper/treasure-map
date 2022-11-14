package com.cxylk.apm.controller;

import com.cxylk.apm.esDao.ApmRepository;
import com.cxylk.apm.esDao.TraceNode;
import com.cxylk.apm.graph.GraphView;
import com.cxylk.apm.graph.GraphViewHelp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author likui
 * @date 2022/11/11 下午2:12
 **/
@Controller
public class GraphControl {
    @Autowired
    private ApmRepository repository;

    @RequestMapping("/monitor")
    public String openMonitorList(Model model){
        List<TraceNode> treeNodes = repository.findByModeType("HttpInfo", PageRequest.of(0, 100));
        model.addAttribute("nodes",treeNodes);
        return "monitorList";
    }

    @RequestMapping("detail/graph/{traceId}")
    @ResponseBody
    public GraphView open(@PathVariable String traceId, Model model) {
        List<TraceNode> nodes = repository.findByTraceId(traceId);
        GraphViewHelp graphViewHelp = new GraphViewHelp(nodes);
        return graphViewHelp.buildGraphView();
    }
}
