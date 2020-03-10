package org.onosproject.oxp.protocol.ver10;

import org.junit.Test;
import org.onosproject.hcp.types.MacAddress;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        Map<String,Set<Integer>> vportmap=new HashMap<>();
        Set<Integer> integers=new HashSet<>();
        integers.add(1);
        integers.add(2);
        vportmap.put("1",integers);

        Set<Integer> integers1=vportmap.get("1");
        if (integers1==null){
            integers1=new HashSet<>();
            vportmap.put("1",integers1);
        }
        integers1.add(3);
        System.out.println(vportmap.get("1").toString());
        Set<Integer> integers2=vportmap.get("1");
        if (integers2!=null){
            integers2.remove(1);
        }
        System.out.println(vportmap.get("1").toString());
    }
}
