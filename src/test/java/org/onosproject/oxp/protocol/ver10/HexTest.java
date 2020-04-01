package org.onosproject.oxp.protocol.ver10;

import org.junit.Test;
import org.onlab.packet.IpAddress;
import org.onosproject.hcp.types.MacAddress;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author ldy
 * @Date: 20-3-3 下午10:37
 * @Version 1.0
 */
public class HexTest {
    @Test
    public  void main() {
        System.out.println(String.format("%06x",2222));
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
}
