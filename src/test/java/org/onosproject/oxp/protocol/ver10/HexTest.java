package org.onosproject.oxp.protocol.ver10;

import org.junit.Test;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.onlab.packet.IpAddress;
import org.onosproject.hcp.types.HCPIOTID;
import org.onosproject.hcp.types.MacAddress;
import org.onosproject.net.PortNumber;
import org.python.modules._systemrestart;
//import org.python.core.PyFunction;
//import org.python.core.PyInteger;
//import org.python.core.PyObject;
//import org.python.util.PythonInterpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author ldy
 * @Date: 20-3-3 下午10:37
 * @Version 1.0
 */
public class HexTest {
    @Test
    public  void main(){
        System.out.println(Integer.parseInt("111"));
        HCPIOTID hcpiotid = HCPIOTID.of("1111");
        HCPIOTID hcpiotid1 =HCPIOTID.of("1111");
        System.out.println(hcpiotid1.equals(hcpiotid));
//        byte b=127;
//        byte c=126;
//        int a=9;
//        Map<PortNumber,Integer> portNumberIntegerMap=new HashMap<>();
//        System.out.println(portNumberIntegerMap.keySet().isEmpty());
////        HashMap<PortNumber,Integer> hashMap=new HashMap<>();
//        PortNumber port=PortNumber.portNumber(1);
//        PortNumber port2=PortNumber.portNumber(2);
//        portNumberIntegerMap.put(port,1);
//        portNumberIntegerMap.put(port2,2);
//        System.out.println(portNumberIntegerMap.toString());
//        System.out.println(new HashMap<>(portNumberIntegerMap));
// hashMap.put(null,null);
//        System.out.println(hashMap.size());
//        System.out.println(b+c);
//        System.out.println(b>>1);
//        String s="pof:000000000000000000000f";
//        Integer integer=Integer.parseInt(s,16);
//        System.out.println(integer);
//        System.out.println(s.replaceFirst("^0*",""));

//        StringBuffer stringBuffer=new StringBuffer();
//        for (int i = 0; i <10 ; i++) {
//            stringBuffer.append(i);
//            stringBuffer.append(",");
//        }
//        stringBuffer.deleteCharAt(stringBuffer.length()-1);
//        System.out.println(stringBuffer.toString());
//        System.out.println(stringBuffer.length());
//        stringBuffer.append("\n");
//        System.out.println(stringBuffer.length());
//        stringBuffer.deleteCharAt(stringBuffer.length()-1);
//        System.out.println(stringBuffer.length());
//        System.out.println(stringBuffer.toString());
//        System.out.println(String.format("%016x",1));
//        System.out.println(System.currentTimeMillis());
//        Set<IpAddress> ipAddresses=new HashSet<>();
//        ipAddresses.add(IpAddress.valueOf("192.168.109.11"));
//        ipAddresses.add(IpAddress.valueOf("192.168.109.12"));
//        ipAddresses.add(IpAddress.valueOf("192.168.109.13"));
//        ipAddresses.add(IpAddress.valueOf("192.168.109.14"));
//        System.out.println(ipAddresses.toArray()[0].toString());
//        System.out.println(String.format("%06x",2222));
//        System.out.println(System.currentTimeMillis());
    }
    @Test
    public void MapTest(){
        Map<String,Map<Integer,Integer>> mapConcurrentHashMap=new HashMap<>();
        Map<Integer,Integer> map=mapConcurrentHashMap.get("1");
        if (map==null){
            map=new HashMap<>();
            mapConcurrentHashMap.put("1",map);
        }
        map.put(1,2);
        map.put(3,4);
        System.out.println(mapConcurrentHashMap.toString());

        Map<String,Set<Integer>> hashMap=new HashMap<>();
        Set<Integer> set=hashMap.get("1");
        if (set==null){
            set=new HashSet<>();
            hashMap.put("1",set);
        }
        set.add(1);
        set.add(2);
        System.out.println(hashMap.toString());
//        Map<String,Set<Integer>> vportmap=new HashMap<>();
//        Set<Integer> integers=new HashSet<>();
//        integers.add(1);
//        integers.add(2);
//        vportmap.put("1",integers);
//
//        Set<Integer> integers1=vportmap.get("1");
//        if (integers1==null){
//            integers1=new HashSet<>();
//            vportmap.put("1",integers1);
//        }
//        integers1.add(3);
//        System.out.println(vportmap.get("1").toString());
//        Set<Integer> integers2=vportmap.get("1");
//        if (integers2!=null){
//            integers2.remove(1);
//        }
//        System.out.println(vportmap.get("1").toString());
//        IpAddress ipAddress=IpAddress.valueOf("10.0.0.1");
//        System.out.println(toHexString(ipAddress).toString());
//        System.out.println(ipAddress.toString());
//        System.out.println(toHex(1));
    }

    private StringBuffer toHexString(IpAddress ipAddress){
        StringBuffer stringBuffer=new StringBuffer();
        byte [] bytes=ipAddress.toOctets();
        for (byte b:bytes){
            stringBuffer.append(toHex(b));
        }
        return stringBuffer;
    }
    char[] map = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    //十进制转16进制
    public String toHex(int num) {
        if(num == 0) return "00";
	   String result = "";
	   while(num != 0){
		int x = num&0xF;
	    result = map[(x)] + result;
	    num = (num >>> 4);
	 }
	 if (num>=16 && result.length()==1){
	       return result+"0";
     }
	 return "0"+result;

    }
//    @Test
//    public  void invoke_python(){
//        System.out.println(System.currentTimeMillis());
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println(System.currentTimeMillis());
//        Process proc;
//        try {
//            System.out.println(System.currentTimeMillis());
//            proc = Runtime.getRuntime().exec("python /home/ldy/python_project/test.py");
////            System.out.println(System.currentTimeMillis());
//            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            System.out.println(System.currentTimeMillis());
//            String line = null;
//            while ((line = in.readLine()) != null) {
//                System.out.println(line);
//            }
//            System.out.println(System.currentTimeMillis());
//            in.close();
//            proc.waitFor();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

//    @Test
//    public void jypython(){
//        PythonInterpreter interpreter = new PythonInterpreter();
//        System.out.println(System.currentTimeMillis());
//        interpreter.execfile("/home/ldy/python_project/test1.py");
//        PyFunction pyFunction=interpreter.get("add",PyFunction.class);
//        PyObject pyObject=pyFunction.__call__(new PyInteger(5),new PyInteger(10));
//        System.out.println(System.currentTimeMillis());
//        System.out.println(pyObject);
//    }

}
