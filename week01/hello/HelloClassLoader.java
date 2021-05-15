package zhuyf.jvm01;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 自定义classloader
 *
 */
public class HelloClassLoader extends ClassLoader {

	public static void main(String[] args) throws Exception {
		
		HelloClassLoader helloClassLoader = new HelloClassLoader();
		Object obj = helloClassLoader.findClass("Hello.xlass").newInstance();
		Method method = obj.getClass().getMethod("hello");
		method.invoke(obj);
	}
	
	@Override
	protected Class<?> findClass(String name) {

		// 获取需要加载的类文件数据
		String filePath = this.getClass().getResource("").getPath()+"/"+name;
		byte[] helloFileByte = null;
		try {
			helloFileByte = this.getBytesByFile(filePath);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if(helloFileByte == null) {
			return null;
		}
		
		// 处理转换文件数据
		helloFileByte = this.transformBytes(helloFileByte);
		
		return this.defineClass("Hello",helloFileByte,0,helloFileByte.length);
		
	}
	
	/**
     * 根据文件路径获取字节数组
     * 
     * @param filePath
     * @return
     * @throws Exception
     */
    public byte[] getBytesByFile(String filePath) throws Exception {
        
    	if (filePath == null || filePath.length() < 1) {
            return null;
        }
        
        FileInputStream fileInput = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream bos = null;
        try {       
        	
            fileInput = new FileInputStream(filePath);
            bis = new BufferedInputStream(fileInput);
            bos = new ByteArrayOutputStream();
            byte[] buf = new byte[512];
            int size;
            while ((size = bis.read(buf)) != -1) {
                bos.write(buf, 0, size);
            }
            return bos.toByteArray();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                }
            }
            if (fileInput != null) {
                try {
                	fileInput.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    /**
     * 获取转换的字节数组
     * @param fileBytes
     * @return
     */
    public byte[] transformBytes(byte[] fileBytes) {
    	
    	if(fileBytes == null) {
    		return null;
    	}
    	
    	for(int i=0;i<fileBytes.length;i++) {
    		int byteData = 255 - convertByteToInt(fileBytes[i]);
    		fileBytes[i] = (byte) (byteData & 0xff);
    	}
    	
    	return fileBytes;
    }
    
    /**
     * 字节转整型
     * @param data
     * @return
     */
    public int convertByteToInt(byte data) {
	    int heightBit = (int) ((data >> 4) & 0x0F);
	    int lowBit = (int) (0x0F & data);
	    return heightBit * 16 + lowBit;
    }
    
}
