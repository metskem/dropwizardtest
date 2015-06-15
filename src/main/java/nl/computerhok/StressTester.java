package nl.computerhok;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class StressTester {
    private static final Logger LOG = LoggerFactory.getLogger(StressTester.class);

    public static long test(int stressFactor, boolean leakMemory, HttpSession session) {
        long startTime = System.currentTimeMillis();
        Vector memoryLeaker = new Vector();
        LOG.debug("using stressFactor " + stressFactor);

        // put a nice string in the session
        session.setAttribute(
                "aLargeString",
                "a b c d e f g h i j k l m n o p q r s t u v w x y z A B C D E F G H I J K L M N O P Q R S T U V W X Y Z    a b c d e f g h i j k l m n o p q r s t u v w x y z A B C D E F G H I J K L M N O P Q R S T U V W X Y Z     a b c d e f g h i j k l m n o p q r s t u v w x y z A B C D E F G H I J K L M N O P Q R S T U V W X Y Z");

        session.setAttribute("a2", new Date());
        session.setAttribute("a3", new Vector());
        session.setAttribute("a4", new Vector());
        session.setAttribute("a5", new Vector());
        session.setAttribute("l5", new Long(123));
        String s = "";
        StringBuffer buff = new StringBuffer("");

        // start the stress loop
        for (int teller1 = 0; teller1 < stressFactor; teller1++) {
            buff = new StringBuffer("");
            for (int i = 0; i < 200; i++) {
                s = "aLargeString"
                        + i
                        + "a b c d e f g h i j k l m n o p q r s t u v w x y z A B C D E F G H I J K L M N O P Q R S T U V W X Y Z    a b c d e f g h i j k l m n o p q r s t u v w x y z A B C D E F G H I J K L M N O P Q R S T U V W X Y Z     a b c d e f g h i j k l m n o p q r s t u v w x y z A B C D E F G H I J K L M N O P Q R S T U V W X Y Z";
                buff.append(s);
            }
        }

        if (leakMemory) {
            memoryLeaker.add(buff);
            int vectorElements = memoryLeaker.size();
            int vectorSize = vectorElements * buff.length();
            int vectorCapacity = memoryLeaker.capacity();
            LOG.debug("using memoryLeak option, memoryLeak Vector has " + vectorElements + " elements , Vector.capacity()=" + vectorCapacity + " , vector occupies " + vectorSize
                    / 1024 / 1024 + " MegaBytes");
        } else {
            LOG.debug("not using memoryLeak option (use memoryLeak=true)");
        }
        HashMap h = new HashMap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(baos);
            //objectOutputStream.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_1);
            objectOutputStream.writeObject(h);
            objectOutputStream.close();
            LOG.debug("just serialized object with length " + baos.size() + " bytes");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (System.currentTimeMillis() - startTime);
    }

}
