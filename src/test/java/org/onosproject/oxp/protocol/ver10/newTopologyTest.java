package org.onosproject.oxp.protocol.ver10;

import com.google.common.collect.ImmutableList;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.junit.Test;
import org.onlab.graph.DefaultEdgeWeigher;
import org.onlab.graph.ScalarWeight;
import org.onlab.graph.Weight;
import org.onlab.packet.ChassisId;
import org.onosproject.common.DefaultTopology;
import org.onosproject.common.DefaultTopologyGraph;
import org.onosproject.hcp.types.DomainId;
import org.onosproject.net.*;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.topology.*;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.stream.Stream;

public class newTopologyTest {
    private List<Device> deviceSet;
    private List<DeviceId> deviceIdSet;
    private Set<Link> linkSet;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    public static final ClusterId C0 = ClusterId.clusterId(0);
    public final  LinkWeigher WEIGHT=new TestWeight();
//    public static final ClusterId C1 = ClusterId.clusterId(1);
    private DomainId domainId=DomainId.of("1");
    private LinkWeigher linkWeigherTool=new TestWeight();
    private ProviderId RouteproviderId=new ProviderId("USTC","HCP");

    private Socket socket;
    private ArrayList<TopologyVertex> topologyVertexArrayList;
    private HashMap<TopologyVertex,List<TopologyEdge>> topologyVertexListHashMap=new HashMap<>();

    @Test
    public void newTopologyTest(){
        try {
            socket=new Socket("192.168.108.100",11000);
            ObjectOutputStream outputStream=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream=new ObjectInputStream(socket.getInputStream());
            new Thread(new start_socket(socket,inputStream,outputStream)).start();
        }
        catch (IOException e){
            e.printStackTrace();
        }

//        init();
//        GraphDescription description=new DefaultGraphDescription(System.nanoTime(),
//                            System.currentTimeMillis(),deviceSet,linkSet);
//        DefaultTopology defaultTopology=new DefaultTopology(ProviderId.NONE,description);
//        topologyVertexArrayList=new ArrayList<>(defaultTopology.getGraph().getVertexes());
//        for (TopologyVertex topologyVertex:topologyVertexArrayList){
//            List<TopologyEdge> topologyEdgeList=new ArrayList<>(defaultTopology.getGraph().getEdgesFrom(topologyVertex));
//            topologyVertexListHashMap.put(topologyVertex,topologyEdgeList);
//        }
//        for (TopologyVertex topologyVertex:topologyVertexArrayList){
//            System.out.println("topologyVertex:"+topologyVertex);
//            System.out.println("Edgelist:"+topologyVertexListHashMap.get(topologyVertex).toString());
//            System.out.println("============================");
//        }

//        writeFile();
//        System.out.println(defaultTopology.getGraph().toString());

//        Set<List<TopologyEdge>> result=new HashSet<>();
////
////        double startTime=System.currentTimeMillis();
////        dfsFindAllRoutes(new DefaultTopologyVertex(deviceIdSet.get(0)),
////                new DefaultTopologyVertex(deviceIdSet.get(3))
////                ,new ArrayList<>(),new ArrayList<>(),defaultTopology.getGraph(),result);
////        result.forEach(linkSet->{
////            System.out.println(linkSet.toString());
////        });
////        System.out.println(System.currentTimeMillis()-startTime);
////        System.out.println("==============================================");
//        BFSFindAllPath(new DefaultTopologyVertex(deviceIdSet.get(0)),
//                        new DefaultTopologyVertex(deviceIdSet.get(3))
//                        ,defaultTopology.getGraph());
//        System.out.println("==============================================");
////        double startTime1=System.currentTimeMillis();
////        BFSFindAllPath1(new DefaultTopologyVertex(deviceIdSet.get(0)),
////                new DefaultTopologyVertex(deviceIdSet.get(3))
////                ,defaultTopology.getGraph());
////        System.out.println(System.currentTimeMillis()-startTime1);
////        Set<TopologyVertex> topologyVertices=defaultTopology.getGraph().getVertexes();
////        TopologyEdge topologyEdge=(TopologyEdge) defaultTopology.getGraph()
////                    .getEdgesFrom((TopologyVertex) topologyVertices.toArray()[0]).toArray()[0];
////        ScalarWeight weight=(ScalarWeight)linkWeigherTool.weight(topologyEdge);
////        double ss=weight.value();
////        System.out.println(ss+10);
//        //
////   System.out.println(defaultTopology.getGraph().getVertexes().toString());
////        Iterator<TopologyVertex> iterator=defaultTopology.getGraph().getVertexes().iterator();
////        while(iterator.hasNext()){
////            TopologyVertex topologyVertex=iterator.next();
////            System.out.println(defaultTopology.getGraph().getEdgesFrom(topologyVertex));
////        }
////        System.out.println("deviceCount:"+defaultTopology.deviceCount());
//        System.out.println(System.currentTimeMillis());
//        Set<Path> disjointPaths=defaultTopology.getPaths(deviceIdSet.get(0),deviceIdSet.get(3));
//        for (Path path:disjointPaths){
//            System.out.println(path.toString());
//        }
//        System.out.println(System.currentTimeMillis());
//        System.out.println("===============================");
//////        System.out.println("path:"+defaultTopology.getDisjointPaths(deviceIdSet.get(1),deviceIdSet.get(3)));
//////        System.out.println("path:"+defaultTopology.getPaths(deviceIdSet.get(1),deviceIdSet.get(3)).toString());
////        Stream<Path> pathList=defaultTopology.getKShortestPaths(deviceIdSet.get(0),deviceIdSet.get(3));
////        pathList.forEach(path -> {
////            System.out.println(path.toString());
////        });
////        System.out.println("==============================");
//        Set<Path> paths=defaultTopology.getPaths(deviceIdSet.get(0),deviceIdSet.get(3),new TestWeight());
//        for (Path path:paths){
//            System.out.println(path.toString());
//        }

////        System.out.println(defaultTopology.getCluster(deviceIdSet.get(1)));
////        System.out.println(defaultTopology.isBroadcastPoint(new ConnectPoint(deviceIdSet.get(1),PortNumber.portNumber(3))));
//        System.out.println(defaultTopology.getClusters().toString());
//        System.out.println(defaultTopology.isInfrastructure(new ConnectPoint(deviceIdSet.get(1),PortNumber.portNumber(10))));
//        TopologyGraph graph=  defaultTopology.getGraph();
//        System.out.println(graph.toString());
//        Set<TopologyEdge> topologyVertices=defaultTopology.getGraph().getEdgesFrom(new DefaultTopologyVertex(deviceIdSet.get(0)));
//        System.out.println(topologyVertices.toString());

//        for (TopologyVertex topologyVertex:defaultTopology.getGraph().getVertexes()){
//            for (TopologyEdge topologyEdge:defaultTopology.getGraph().getEdgesTo(topologyVertex)){
//                System.out.println(topologyEdge);
//            }
//        }
////        System.out.println(defaultTopology.getGraph());
//        defaultTopology.getGraph().getVertexes().forEach(topologyVertex -> {
//            System.out.println(topologyVertex.deviceId());
//        });
//        for (Link Link:defaultTopology.getClusterLinks(defaultTopology.getCluster(ClusterId.clusterId(0)))){
//            System.out.println(Link.toString());
//        }

//        Set<Path> paths;
//        paths=defaultTopology.getPaths(deviceIdSet.get(0),deviceIdSet.get(2));
//        System.out.println(paths.toString());
//        System.out.println(((Path)paths.toArray()[0]).links().size());
////        System.out.println(paths.count());
//        paths.forEach(path -> {
//            path.links().forEach(link -> {
//                System.out.println(link.toString());
//            });
//            System.out.println("===========================================");
////            System.out.println(path.toString());
//        });

//        System.out.println(new ScalarWeight(0.0D).toString());
//        Set<Path> paths=new HashSet<>();
//        paths=defaultTopology.getPaths(deviceIdSet.get(0),deviceIdSet.get(3),WEIGHT,-1);
//        System.out.println(paths.size());
//        paths.forEach(path -> {
////            path.links().forEach(link -> {
////                System.out.println(link.toString());
////            });
////            System.out.println("===========================================");
//            System.out.println(path.toString());
//        });
    }
    class start_socket implements Runnable{
        private Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        start_socket(Socket socket,ObjectInputStream inputStream,ObjectOutputStream outputStream){
            this.socket=socket;
            this.inputStream=inputStream;
            this.outputStream=outputStream;
        }
        @Override
        public void run() {
            try {
                String message="1";
                System.out.println(message.getBytes());
                outputStream.write(message.getBytes());
                outputStream.flush();
               while(true){
                   String mesage= String.valueOf(inputStream.readByte());
                   System.out.println(mesage);
                   if (mesage.equals("2")){
                       break;
                   }
               }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    private void writeFile() {
        BufferedWriter bufferedWriter=null;
        StringBuffer stringBuffer=new StringBuffer();
        try {
            File writename =new File("/home/ldy/topology.txt");
            bufferedWriter =new BufferedWriter(new FileWriter(writename));
            for (TopologyVertex topologyVertex:topologyVertexArrayList) {
                stringBuffer.append(topologyVertex.toString().split(":")[1].replaceFirst("^0*",""));
                for(TopologyEdge topologyEdge:topologyVertexListHashMap.get(topologyVertex)){
                    stringBuffer.append(","+topologyEdge.dst().toString().split(":")[1].replaceFirst("^0*",""));
                }
                stringBuffer.append(":"+"10"+"\n");
            }
           bufferedWriter.write(stringBuffer.toString());
           bufferedWriter.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
     class TestWeight extends DefaultEdgeWeigher<TopologyVertex,TopologyEdge>
                                implements LinkWeigher{
         private int cont=0;
        @Override
        public Weight weight(TopologyEdge edge) {
            return ScalarWeight.NON_VIABLE_WEIGHT;
        }
    }

    public void init(){
       deviceIdSet=new ArrayList<>();
       deviceSet=new ArrayList<>();
       linkSet=new HashSet<>();
       DeviceIdSet();
       DeviceSet(deviceIdSet);
       InterLink();
    }

    public List<Device> DeviceSet(List<DeviceId> deviceIds){
        for (DeviceId deviceId:deviceIds){
            Device device=new DefaultDevice(ProviderId.NONE,deviceId, Device.Type.CONTROLLER
                            ,"USTC","1.0","1.0","001",new ChassisId(deviceId.toString().substring("hcp:".length())));
            deviceSet.add(device);
        }
        return deviceSet;
    }

    public void DeviceIdSet(){
        for (int i = 1; i <=10; i++) {
            DeviceId deviceId=DeviceId.deviceId("hcp:"+String.format("%016x",i));
            deviceIdSet.add(deviceId);
        }

    }

    public void InterLink(){
        for (int i = 0; i <10 ; i++) {
            if (i!=9){
                ConnectPoint srcConnec=new ConnectPoint(deviceIdSet.get(i),PortNumber.portNumber(0));
                ConnectPoint dstConnec=new ConnectPoint(deviceIdSet.get(i+1),PortNumber.portNumber(1));
                Link link=getLink(srcConnec,dstConnec);
                linkSet.add(link);
                Link link1=getLink(dstConnec,srcConnec);
                linkSet.add(link1);
            }else{
                ConnectPoint srcConnec=new ConnectPoint(deviceIdSet.get(i),PortNumber.portNumber(0));
                ConnectPoint dstConnec=new ConnectPoint(deviceIdSet.get(0),PortNumber.portNumber(1));
                Link link=getLink(srcConnec,dstConnec);
                linkSet.add(link);
                Link link1=getLink(dstConnec,srcConnec);
                linkSet.add(link1);
            }
        }
        // add a link to deviceId2 and deviceId6
        ConnectPoint srcConnec=new ConnectPoint(deviceIdSet.get(1),PortNumber.portNumber(2));
        ConnectPoint dstConnec=new ConnectPoint(deviceIdSet.get(5),PortNumber.portNumber(2));
        Link link=getLink(srcConnec,dstConnec);
        linkSet.add(link);
        Link link1=getLink(dstConnec,srcConnec);
        linkSet.add(link1);

        //add  a link to deviceId3 and deviceId5
        ConnectPoint src=new ConnectPoint(deviceIdSet.get(2),PortNumber.portNumber(2));
        ConnectPoint dst=new ConnectPoint(deviceIdSet.get(4),PortNumber.portNumber(2));
        Link link2=getLink(src,dst);
        Link link3=getLink(dst,src);
        linkSet.add(link2);
        linkSet.add(link3);

    }

    public Link getLink(ConnectPoint srcConn,ConnectPoint dstConn){
        Link link=DefaultLink.builder()
                .src(srcConn)
                .dst(dstConn)
                .state(Link.State.ACTIVE)
                .type(Link.Type.DIRECT)
                .providerId(ProviderId.NONE)
                .build();
        return link;

    }
    @Test
    public void StringTest(){
        DeviceId deviceId=DeviceId.deviceId("hcp:"+String.format("%016x",1));
        System.out.printf(deviceId.toString());
    }

    private void BFSFindAllPath(TopologyVertex src,TopologyVertex dst,TopologyGraph graph){
        if (src.equals(dst)){
            return;
        }
        Queue<List<TopologyVertex>> path=new LinkedList<>();
        Set<List<TopologyVertex>> result=new HashSet<>();
        List<TopologyVertex> list=new ArrayList<>();
        list.add(src);
        path.offer(list);
        while(!path.isEmpty()){
            List<TopologyVertex> vertexList=path.poll();
            TopologyVertex vertex=vertexList.get(vertexList.size()-1);
            if (vertex.equals(dst)){
                result.add(ImmutableList.copyOf(vertexList));
                continue;
            }
//            visitMap.put(vertex,true);
            Iterator<TopologyEdge> iterator=graph.getEdgesFrom(vertex).iterator();
            while(iterator.hasNext()){
                List<TopologyVertex> vertexList1=new ArrayList<>(vertexList);
                TopologyVertex vertex1=iterator.next().dst();
                if (!vertexList1.contains(vertex1)){
                    vertexList1.add(vertex1);
                    path.add(vertexList1);
                }
            }

        }
        result.forEach(ll->{
            System.out.println(ll.toString());
        });

    }
    private void BFSFindAllPath1(TopologyVertex src,TopologyVertex dst,TopologyGraph graph){
        if (src.equals(dst)){
            return;
        }
        Queue<List<TopologyEdge>> path=new LinkedList<>();
        Set<List<TopologyEdge>> result=new HashSet<>();
        List<TopologyEdge> list;
        Iterator<TopologyEdge> iterator=graph.getEdgesFrom(src).iterator();
        while(iterator.hasNext()){
            list=new ArrayList<>();
            list.add(iterator.next());
            path.add(list);
        }
        while(!path.isEmpty()){
            List<TopologyEdge> edgeList=path.poll();
            TopologyEdge edge=edgeList.get(edgeList.size()-1);
            TopologyVertex vertex=edge.dst();
            if (edge.dst().equals(dst)){
                result.add(ImmutableList.copyOf(edgeList));
                continue;
            }
            Iterator<TopologyEdge> iterator1=graph.getEdgesFrom(vertex).iterator();
            while(iterator1.hasNext()){
                List<TopologyEdge> edgeList1=new ArrayList<>(edgeList);
                TopologyEdge edge1=iterator1.next();
                TopologyVertex vertex1=edge1.dst();
                edgeList1.add(edge1);
                path.add(edgeList1);
                for (TopologyEdge topologyEdge:edgeList1){
                    if (topologyEdge.src().equals(vertex1)){
                        path.remove(edgeList1);
                        break;
                    }
                }
            }

        }
        result.forEach(ll->{
            System.out.println(ll.toString());
        });

        System.out.println("============================");
        Set<Path> pathSet=CalculatePathCost(result);
        Path path1=selectPath(pathSet);
        System.out.println(path1.src());
        System.out.println(path1.dst());
        path1.links().forEach(link -> {
            System.out.println(link);
        });
        System.out.println(path1.toString());

    }

    /***
     * 返回得到最优的path
     * @param paths
     * @return
     */
    private  Path selectPath(Set<Path> paths){
        if (paths.size()<1){
            return null;
        }
        return getMinCostPath(new ArrayList(paths) );
    }

    private Path getMinCostPath(List<Path> pathList){
        pathList.sort((p1,p2)->((ScalarWeight)p1.weight()).value()>((ScalarWeight)p2.weight()).value()
                ? 1:(((ScalarWeight)p1.weight()).value()<((ScalarWeight)p2.weight()).value())? -1:0);
        pathList.forEach(path -> {
            System.out.println(path);
        });
        System.out.println("===================================");
        return (Path)pathList.toArray()[0];
    }

    /**
     * 获取每条路径的权重，首先，计算路径上的每一条链路的权值,其次，选择路径上所有链路的权值的最大值，作为该路径的带宽权值
     * 因为各条链路的负载不同，根据木桶效应，具有最大负载的链路将成为整条路径的短板.
     * @param pathSet
     * @return
     */
    private Set<Path> CalculatePathCost(Set<List<TopologyEdge>> pathSet){
        Set<Path> allResult=new HashSet<>();

        pathSet.forEach(path->{
            ScalarWeight weight=(ScalarWeight) maxPathWeight(path);
            allResult.add(parseEdgetoLink(path,weight));
        });

        return allResult;
    }

    /**
     *  计算路径上的每一条链路的权值,选择路径上所有链路的权值的最大值
     * @param edgeList
     * @return
     */
    private Weight maxPathWeight(List<TopologyEdge> edgeList){
        double weight=0;
        for (TopologyEdge edge:edgeList){
            ScalarWeight scalarWeight=(ScalarWeight) linkWeigherTool.weight(edge);
            double linkWeight=scalarWeight.value();
            weight=Double.max(weight,linkWeight);
        }
        return new ScalarWeight(weight);
    }

    /**
     * 将每条路径的Edge属性改成link，并添加Cost属性
     * @param edgeList
     * @param cost
     * @return
     */
    private Path parseEdgetoLink(List<TopologyEdge> edgeList,ScalarWeight cost){
        List<Link> links=new ArrayList<>();
        edgeList.forEach(edge -> {
            links.add(edge.link());
        });
        return new DefaultPath(RouteproviderId,links,cost);
    }

    private void dfsFindAllRoutes(TopologyVertex src,
                                  TopologyVertex dst,
                                  List<TopologyEdge> passedLink,
                                  List<TopologyVertex> passedDevice,
                                  TopologyGraph topoGraph,
                                  Set<List<TopologyEdge>> result) {
        if (src.equals(dst))
            return;

        passedDevice.add(src);

        Set<TopologyEdge> egressSrc = topoGraph.getEdgesFrom(src);
        egressSrc.forEach(egress -> {
            TopologyVertex vertexDst = egress.dst();
            if (vertexDst.equals(dst)) {
                passedLink.add(egress);
                result.add(ImmutableList.copyOf(passedLink.iterator()));
                passedLink.remove(egress);

            } else if (!passedDevice.contains(vertexDst)) {
                passedLink.add(egress);
                dfsFindAllRoutes(vertexDst, dst, passedLink, passedDevice, topoGraph, result);
                passedLink.remove(egress);

            } else {

            }
        });

        passedDevice.remove(src);
    }
}
