package org.onosproject.hcp.util;

import org.onosproject.net.packet.InboundPacket;

public class HexString {


    /**
     * String value to long value
     * @param value
     * @return long
     */
    public static long toLong(String value) throws NumberFormatException{
       String octets[]=value.split(":");
       if (octets.length>8){
           throw new NumberFormatException("Input string is to big to fit in long :"+value);
       }
       long longvalue=0;
       for (String oct:octets){
           if (oct.length()>2){
               throw new  NumberFormatException("Each colon-separated byte component must consist of 1 or 2 hex digits: " + value);
           }
           short s=Short.parseShort(oct,16);
           longvalue=(longvalue<<8)+s;

       }
       return longvalue;
    }
    public static String toHexString(final  long value,final int padTo){
      char arr[]=Long.toHexString(value).toCharArray();
      String ret="";
      int i=0;
      for (;i<(padTo*2-arr.length);i++){
          ret+="0";
          if ((i%2)!=0)
              ret+=":";
      }
      for (int j = 0; j <arr.length ; j++) {
          ret += arr[j];
          if ((((i + j) % 2) != 0) && (j < (arr.length - 1))) {
              ret += ":";
          }
      }
      return ret;
    }
    public static String toHexString(final long value){
        return toHexString(value,8);
    }

    public static String parseInboundPacket(InboundPacket inboundPacket, int index, int length){
        String result = "";
        for (int i = 0; i < length; i++) {
            result += String.format("%02x",inboundPacket.unparsed().get(index + i)&0xff);
        }
        return result;
    }
}
