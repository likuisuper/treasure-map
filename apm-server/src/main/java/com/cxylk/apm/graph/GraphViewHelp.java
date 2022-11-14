package com.cxylk.apm.graph;

import com.cxylk.apm.esDao.TraceNode;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class GraphViewHelp {
    Collection<TraceNode> traceNodes;



    public GraphViewHelp(Collection<TraceNode> nodes) {
        this.traceNodes = nodes
                .stream()
                .filter(n -> !"server".equals(n.getSeat()))
                .collect(Collectors.toList());

    }

    /**
     * 1、统计出所有节点(Http起点、应用节点、远程节点、数据库节点)
     *
     * @return
     */
    public GraphView buildGraphView() {
        GraphView graphView = new GraphView();
        List<GraphView.Nodes> nodes = new ArrayList<>();
        List<GraphView.Edges> edges = new ArrayList<>();


        // 构建根节点
        TraceNode rootNode = getTraceNode("0");
        Assert.isTrue(rootNode.getModeType().equals("HttpInfo") , "当前仅支持 HttpTraceNode 做为入口节点");
        GraphView.Nodes rootGraphNode = new GraphView.Nodes();
        rootGraphNode.setId("root");
        rootGraphNode.setTitle("浏览器");
        rootGraphNode.setSubTitle(rootNode.getClientIp());
        rootGraphNode.setType("browser");
        rootGraphNode.setState(StringUtils.hasText(rootNode.getError())? "error" : "ok");
        rootGraphNode.setTips(rootNode.getUrl());
        nodes.add(rootGraphNode);
        graphView.setShowDefaultNode(rootGraphNode);
        graphView.setTitle(rootNode.getUrl());

        // 构建 Node 节点
        Collection<GraphView.Nodes> normalNodes = traceNodes.stream()
                .map(a -> buildGraphNode(a))
                .filter(a -> a != null)
                .collect(Collectors.toMap(k -> k.getId(), v -> v, this::merge))// 去重
                .values();
        nodes.addAll(normalNodes);

        // 构建关系
        Collection<GraphView.Edges> normalEdges = traceNodes.stream()
                .map(a -> buildEdge(a))
                .filter(a -> a != null)
                .collect(Collectors.toMap(k -> k.getFrom() + k.getTo(), v -> v, (v1, v2) -> v1)).values();
        edges.addAll(normalEdges);

        graphView.setNodes(nodes);
        graphView.setEdges(edges);

        return graphView;
    }

    // 合并Tips
    private GraphView.Nodes merge(GraphView.Nodes node1, GraphView.Nodes node2) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(node1.getTips()))
            sb.append(node1.getTips());
        if (StringUtils.hasText(node2.getTips())) { // 去重
            sb.append("<br>");
            sb.append(node2.getTips());
        }
        if (sb.toString().startsWith("<br>")) {
            sb = sb.delete(0, 4);
        }
        if (StringUtils.hasText(sb.toString())) {
            // 排序 并去重
            String tips = Arrays.stream(sb.toString().split("<br>")).distinct().sorted().collect(Collectors.joining("<br>"));
            node1.setTips(tips);
        }
        return node1;
    }

    private GraphView.Edges buildEdge(TraceNode traceNode) {
        GraphView.Edges edge = new GraphView.Edges();
        if (traceNode.getModeType().equals("HttpInfo") && traceNode.getSpanId().equals("0")) {
            edge.setFrom("root");
            edge.setTo(buildGraphNode(traceNode).getId());
            edge.setLabel("http请求");
            edge.setType("http request");
            edge.setCount(1);
        } else if (traceNode.getModeType().equals("DubboInfo")) {
            // 获取父节点对应的Graph ID
            edge.setFrom(buildGraphNode(getTraceNode(getParentId(traceNode.getSpanId()))).getId());
            edge.setTo(buildGraphNode(traceNode).getId());
            edge.setLabel("dubbo调用");
            edge.setType("dubbo invoker");
            edge.setCount(1);
        } else if (traceNode.getModeType().equals("SqlInfo")) {
            // 获取父节点对应的Graph ID
            edge.setFrom(buildGraphNode(getTraceNode(getParentId(traceNode.getSpanId()))).getId());
            edge.setTo(buildGraphNode(traceNode).getId());
            edge.setLabel("sql执行");
            edge.setType("sql invoker");
            edge.setCount(1);
        } else {
            edge = null;
        }
        return edge;
    }

    private TraceNode getTraceNode(String nodeId) {
        return traceNodes.stream().filter(a -> a.getSpanId().equals(nodeId)).findFirst().get();
    }


    public static String getParentId(String nodeId) {
        // 没有父节点
        if (nodeId.equals("0")) {
            return null;
        }
        Assert.isTrue(nodeId.indexOf(".") > -1, "非法的nodeId nodeId=" + nodeId);
        return nodeId.substring(0, nodeId.lastIndexOf("."));
    }

    public static int compareNodeId(String nodeId1, String nodeId2) {
        Assert.hasText(nodeId1, "参数nodeId1不能为空");
        Assert.hasText(nodeId2, "参数nodeId2不能为空");
        /*if (nodeId1.equals(nodeId2)) {
            return 0;
        }
        String[] ids1 = nodeId1.split("\\.");
        String[] ids2 = nodeId2.split("\\.");
        for (int i = 0; i <Math.min(ids1.length,ids2.length) ; i++) {
            if (Integer.parseInt(ids1[i]) > Integer.parseInt(ids2[i])) {
                return 1;
            }
        }
        return ids1.length - ids2.length;*/
        return nodeId1.compareTo(nodeId2);
    }


    private GraphView.Nodes buildGraphNode(TraceNode traceNode) {
        GraphView.Nodes graphViewNode = new GraphView.Nodes();
        String url, id;
        int port;
        URI uri;
        if (traceNode .getModeType().equals("HttpInfo")) {
            url = traceNode.getUrl();
            uri = buildURI(traceNode, url);
            port = uri.getPort() == -1 ? 80 : uri.getPort();
            id = uri.getHost() + "@" + port;
            graphViewNode.setId(id);
            graphViewNode.setSubTitle(id);
            graphViewNode.setTitle(traceNode.getAppName());
            graphViewNode.setType("http server"); // Http服务
            graphViewNode.setState(( traceNode).getError() == null ? "normal" : "error");
        } else if (traceNode .getModeType().equals("DubboInfo")) {
            url = traceNode.getRemoteUrl();
            uri = buildURI(traceNode, url);
            id = uri.getHost() + "@" + uri.getPort();
            graphViewNode.setId(id);
            graphViewNode.setSubTitle(id);
            graphViewNode.setTips(getSimpleClassName(traceNode.getServiceInterface()) + "#" + traceNode.getServiceMethodName());
            String name = traceNodes.stream()
                    .filter(a -> a.getSpanId().equals(traceNode.getSpanId() + ".remote"))
                    .findAny()
                    .map(a -> a.getAppName())
                    .orElseGet(() -> {
                        URI remoteUri = GraphViewHelp.buildURI(traceNode, traceNode.getRemoteUrl());
                        return remoteUri.getHost() + "@" + remoteUri.getPort();
                    });

            graphViewNode.setTitle(name);

            graphViewNode.setType("dubbo server");// dubbo 服务
            graphViewNode.setState(traceNode.getError() == null ? "normal" : "error");
        } else if (traceNode.getModeType().equals("SqlInfo")) {
//            SqlTraceNode sqlNode = (SqlTraceNode) traceNode;
            url = traceNode.getJdbcUrl();
            url = url.replaceFirst("jdbc:", "");
            uri = buildURI(traceNode, url);
            id = uri.getHost() + "@" + uri.getPort() + uri.getPath(); // ip host database name
            graphViewNode.setId(id);
            graphViewNode.setSubTitle(uri.getHost() + "@" + uri.getPort());
            graphViewNode.setTitle(uri.getPath().substring(1));
            graphViewNode.setType("database");
            graphViewNode.setState((traceNode).getError() == null ? "normal" : "error");
            SqlStatParse parse = new SqlStatParse(traceNode.getSql(), "");
            String tip = parse.getAll().stream().map(a -> a.getModel() + " -> " + a.getTableName()).distinct().collect(Collectors.joining("<br>"));
            graphViewNode.setTips(tip);
        } else {
            graphViewNode = null;
        }
        return graphViewNode;
    }

    public static URI buildURI(TraceNode node, String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("url 无法正常解析 url=%s。traceId=%s。nodeId=%s", url, node.getTraceId(), node.getSpanId()), e);

        }
    }
    public static String getSimpleClassName(String className) {
        className=className.replaceAll("/",".");
        if (className.lastIndexOf(".") > 0) {
            return className.substring(className.lastIndexOf(".") + 1);
        }
        return className;
    }
}
