package cn.smbms.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    public static Properties properties;
    public static ConfigManager configManager=null;
//    public static ConfigManager configManager=new ConfigManager(); 饿汉模式
    private  ConfigManager(){
         String configFile="database.properties";
        properties =new Properties();
        InputStream is=ConfigManager.class.getClassLoader().getResourceAsStream(configFile);
        try{
            properties.load(is);
            is.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

//    public static Class ConfigManagerHelper{
//        private static  final  ConfigManager INSTEANCE=new ConfigManager();
//    }


    //加synchronized修饰  懒汉模式
    public  static ConfigManager getInstance(){
        if(configManager==null){
            configManager=new ConfigManager();
        }
        return configManager;
    }

    public String getValue(String key){
        return properties.getProperty(key);
    }
}
