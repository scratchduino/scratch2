package scratchduino.robot.rest.v1;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Context;
import org.apache.commons.logging.*;
import scratchduino.robot.*;



@Path("/")
public class FEConnector{
   private static Log log = LogFactory.getLog(FEConnector.class);
   private static final String LOG = "[Connector] ";
   

   @Context
   ServletContext context;

   
   private static final Map<String,String > locks = new HashMap<String, String>();
   
   
   private static final AtomicLong atomlongCID = new AtomicLong(0);



   public FEConnector(@Context HttpServletRequest hsr){
      log.info(LOG + "\n------------------------------------\n" + hsr.getRequestURI());

   }


   
   @GET
   @Path("/crossdomain.xml")
   @Produces("text/plain; charset=UTF-8")
   public String crossdomain() throws Exception{      
      return "<?xml version=\"1.0\"?><cross-domain-policy><site-control permitted-cross-domain-policies=\"master-only\"/><allow-access-from domain=\"*\" /></cross-domain-policy>";
   }
   @POST
   @Path("/crossdomain.xml")
   @Produces("text/plain; charset=UTF-8")
   public String crossdomainPOST() throws Exception{      
      log.trace(LOG + "POST");
      return crossdomain();
   }



   
   
   
   
   
   @GET
   @Path("/txt/list")
   @Produces("text/plain; charset=UTF-8")
   public String listTxt() throws Exception{
      
      IDeviceLocator locator = ((IDeviceLocator) context.getAttribute("locator"));
      
      StringBuffer sb = new StringBuffer();
      
      if(locator.getStatus() == IDeviceLocator.STATUS.READY){
         for(IPort port : locator.getPortList()){
            if(port.getProgress() == IPort.PROGRESS.ROBOT_DETECTED){
               if(sb.length() > 0) sb.append("\n");
               sb.append(port.getDevice().getType());
            }
         }
      }
      
      return sb.toString();
   }
   
   
   
   
   
   
   
   
   

   
   @GET
   @Path("/txt/def/{DEVICE}/{paths:.+}")
   @Produces("text/plain; charset=UTF-8")
   public String defaultPortTxt(@PathParam("PORT")   String sPortName,
                                @PathParam("DEVICE") int iDeviceID,
                                @PathParam("paths")  List<PathSegment> uglyPath,
                                @Context HttpServletResponse response) throws Exception{
      resetCacheAndAllowAccess(response);      
      
      IDeviceLocator locator = ((IDeviceLocator) context.getAttribute("locator"));
      
      log.debug(LOG + "Status=" + locator.getStatus());
      
      if(locator.getStatus() == IDeviceLocator.STATUS.READY){
         for(IPort port : locator.getPortList()){
            if(port.getProgress() == IPort.PROGRESS.ROBOT_DETECTED && port.getDevice().getType() == iDeviceID){
               return servicePortTxt(port.getPortName(), iDeviceID, uglyPath, response);
            }
         }
      }
      
      return "error=1";
   }
   @POST
   @Path("/txt/def/{DEVICE}/{paths:.+}")
   @Produces("text/plain; charset=UTF-8")
   public String defaultPortPOSTTxt(@PathParam("PORT")    String sPortName,
                                    @PathParam("DEVICE")  int iDeviceID,
                                    @PathParam("paths") List<PathSegment> uglyPath,
                                    @Context HttpServletResponse response) throws Exception{
      log.trace(LOG + "POST");
      
      resetCacheAndAllowAccess(response);      
      
      return defaultPortTxt(sPortName, iDeviceID, uglyPath, response);
   }
   
   
   
   
   


   @GET
   @Path("/txt/port/{PORT}/{DEVICE}/{paths:.+}")
   @Produces("text/plain; charset=UTF-8")
   public String servicePortTxt(@PathParam("PORT")  String sPortName,
                                @PathParam("DEVICE")  int iDeviceID,
                                @PathParam("paths") List<PathSegment> uglyPath,
                                @Context HttpServletResponse response) throws Exception{
      
      resetCacheAndAllowAccess(response);      

      List<String> listParameters = new ArrayList<String>();

      for(PathSegment pathSegment : uglyPath){
         listParameters.add(pathSegment.getPath());
      }
      
      String sCommand = listParameters.remove(0);
      
      
      synchronized(FEConnector.class){
         if(locks.get(sPortName) == null){
            locks.put(sPortName, sPortName);
         }
      }
      
      synchronized(locks.get(sPortName)){      
      
         IDeviceLocator locator = ((IDeviceLocator) context.getAttribute("locator"));
         
         if(locator.getStatus() == IDeviceLocator.STATUS.READY){      
            if("crossdomain.xml".equals(sCommand)){
               return "<?xml version=\"1.0\"?><cross-domain-policy><site-control permitted-cross-domain-policies=\"master-only\"/><allow-access-from domain=\"*\" /></cross-domain-policy>";
            }
            else{
               try{
                  
                  if(locator.getPortByName().get(sPortName).getDevice().getType() == iDeviceID) { 
                     IDeviceList listDevices = (IDeviceList) context.getAttribute("devices");
                     ICommand command = listDevices.getDevice(iDeviceID).getCommand(sCommand);
                     
                     if(command == null){
                        return "error=" + IRest.UNKNOWN_COMMAND;
                     }                  
                     
                     IResponse reponse = command.run(atomlongCID.getAndIncrement(), locator.getPortByName().get(sPortName), listParameters);
   
                     StringBuilder sb = new StringBuilder();
                     ArrayList<String> arliKeys = new ArrayList<String>(reponse.getParsedValues().keySet());
                     
//                        Collections.sort(arliKeys);
   
                     for(String sKey : arliKeys){
                        //sb.append(sKey + "=" + reponse.getParsedValues().get(sKey) + "\n");
                        Object value = reponse.getParsedValues().get(sKey);
                        
                        if(value instanceof byte[]) {
                           StringBuffer sbArray = new StringBuffer();
                           for(byte element : (byte[]) value){
                              if(sbArray.length() > 0) sbArray.append(",");
                              sbArray.append((int) element & 0xff);
                           }
                           sbArray.append("\n");
                           sb.append(sbArray);
                        }
                        else {
                           sb.append(reponse.getParsedValues().get(sKey) + "\n");
                        }
                     }
   
                     return sb.toString();
                  }
                  else{
                     return "error=" + IRest.DEVICE_NOT_SUPPORTED;
                  }
                  }
               catch (Exception e){
                  return "error=" + IRest.UNKNOWN_ERROR;
               }
            }
         }
         else{
            return "error=1";
         }
      }
   }
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   @GET
   @Path("/bin/def/{DEVICE}/{paths:.+}")
   @Produces(MediaType.APPLICATION_OCTET_STREAM)   
   public byte[] defaultPortBin(@PathParam("PORT")   String sPortName,
                                @PathParam("DEVICE") int iDeviceID,
                                @PathParam("paths")  List<PathSegment> uglyPath,
                                @Context HttpServletResponse response) throws Exception{
      resetCacheAndAllowAccess(response);      
      
      IDeviceLocator locator = ((IDeviceLocator) context.getAttribute("locator"));
      
      log.debug(LOG + "Status=" + locator.getStatus());
      
      if(locator.getStatus() == IDeviceLocator.STATUS.READY){
         for(IPort port : locator.getPortList()){
            if(port.getProgress() == IPort.PROGRESS.ROBOT_DETECTED && port.getDevice().getType() == iDeviceID){
               return servicePortBin(port.getPortName(), iDeviceID, uglyPath, response);
            }
         }
      }
      
      return new byte[0];
   }
   @POST
   @Path("/bin/def/{DEVICE}/{paths:.+}")
   @Produces(MediaType.APPLICATION_OCTET_STREAM)   
   public byte[] defaultPortPOSTBin(@PathParam("PORT")    String sPortName,
                                    @PathParam("DEVICE")  int iDeviceID,
                                    @PathParam("paths") List<PathSegment> uglyPath,
                                    @Context HttpServletResponse response) throws Exception{
      log.trace(LOG + "POST");
      
      resetCacheAndAllowAccess(response);      
      
      return defaultPortBin(sPortName, iDeviceID, uglyPath, response);
   }
   
   
   
   
   


   @GET
   @Path("/bin/port/{PORT}/{DEVICE}/{paths:.+}")
   @Produces(MediaType.APPLICATION_OCTET_STREAM)   
   public byte[] servicePortBin(@PathParam("PORT")  String sPortName,
                                @PathParam("DEVICE")  int iDeviceID,
                                @PathParam("paths") List<PathSegment> uglyPath,
                                @Context HttpServletResponse response) throws Exception{
      
      resetCacheAndAllowAccess(response);      

      List<String> listParameters = new ArrayList<String>();

      for(PathSegment pathSegment : uglyPath){
         listParameters.add(pathSegment.getPath());
      }
      
      String sCommand = listParameters.remove(0);
      
      
      synchronized(FEConnector.class){
         if(locks.get(sPortName) == null){
            locks.put(sPortName, sPortName);
         }
      }
      
      synchronized(locks.get(sPortName)){      
      
         IDeviceLocator locator = ((IDeviceLocator) context.getAttribute("locator"));
         
         if(locator.getStatus() == IDeviceLocator.STATUS.READY){      
            try{
               if(locator.getPortByName().get(sPortName).getDevice().getType() == iDeviceID) { 
                  IDeviceList listDevices = (IDeviceList) context.getAttribute("devices");
                  ICommand command = listDevices.getDevice(iDeviceID).getCommand(sCommand);
                  
                  if(command == null){
                     return new byte[0];
                  }                  
                  
                  IResponse reponse = command.run(atomlongCID.getAndIncrement(), locator.getPortByName().get(sPortName), listParameters);

                  ArrayList<String> arliKeys = new ArrayList<String>(reponse.getParsedValues().keySet());


                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  

                  for(String sKey : arliKeys){
                     //sb.append(sKey + "=" + reponse.getParsedValues().get(sKey) + "\n");
                     Object value = reponse.getParsedValues().get(sKey);
                     
                     if(value instanceof byte[]){
                        baos.write((byte[]) value);
                     }
                     else if(value instanceof Integer){
                        Integer valueInteger = (Integer) value;
                        baos.write((valueInteger >> 8) & 0xFF);
                        baos.write(valueInteger & 0xFF);
                     }
                     else if(value instanceof Byte){
                        baos.write(new byte[] {(Byte) value});
                     }
                     else{
                        throw new Error("Unsupported response format");
                     }
                  }

                  return baos.toByteArray();
               }
               else{
                  return new byte[0];
               }
            }
            catch (Exception e){
               return new byte[0];
            }
         }
         else{
            return new byte[0];
         }
      }
   }
   
   
   
   
   
   
   
   
   
   
   private void resetCacheAndAllowAccess(HttpServletResponse response){
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");
      response.setHeader("Access-Control-Allow-Origin", "*");      
   }
   
   
   

   
   
   @GET
   @Path("/settings")
   @Produces("text/plain; charset=UTF-8")
   public String settings() throws Exception{      
      log.trace(LOG + "settings()");

      final IConfiguration config = scratchduino.robot.Context.ctx.getBean("config", IConfiguration.class);

      return "default_motor_speed=" + config.getDefaultMotorSpeed();
   }
   
   
   
   
   
   
   @GET
   @Path("/dialog/load/reset")
   @Produces("text/plain; charset=UTF-8")
   public String dialogLoadReset() throws Exception{      
      log.trace(LOG + "loadReset()");

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      main.dialogLoadReset();

      return "";
   }
   
   

   
   @GET
   @Path("/dialog/load/check")
   @Produces("text/plain; charset=UTF-8")
   public String dialogLoadCheck() throws Exception{      
      log.trace(LOG + "loadCheck()");

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      return main.dialogLoadCheck() == null ? "" : main.dialogLoadCheck();  
   }
   
   
   
   @GET
   @Path("/dialog/load/data")
   @Produces(MediaType.APPLICATION_OCTET_STREAM)   
   public byte[] dialogLoadData() throws Exception{      
      log.trace(LOG + "loadData()");

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      return main.dialogLoadData();  
   }
   
   

   
   @GET
   @Path("/dialog/save/reset")
   @Produces("text/plain; charset=UTF-8")
   public String dialogSaveReset() throws Exception{      
      log.trace(LOG + "loadReset()");

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      main.dialogSaveReset();

      return "";
   }
   
   

   
   @GET
   @Path("/dialog/save/check")
   @Produces("text/plain; charset=UTF-8")
   public String dialogSaveCheck() throws Exception{      
      log.trace(LOG + "loadCheck()");

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      return main.dialogSaveCheck() == null ? "" : main.dialogSaveCheck();  
   }
   
   
   @POST
   @Path("/dialog/save")
   @Produces("text/plain; charset=UTF-8")
   public String saveScratch(byte[] data) throws Exception{      
      log.trace(LOG + "SAVE length=" + data.length);

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      main.dialogSave(data);
      
      return "";
   }
   @POST
   @Path("/dialog/save_tmp")
   @Produces("text/plain; charset=UTF-8")
   public String saveScratchTmp(byte[] data) throws Exception{      
      log.trace(LOG + "SAVE length=" + data.length);

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      main.dialogSaveTmp(data);
      
      return "";
   }
   @POST
   @Path("/dialog/save/{NAME}")
   @Produces("text/plain; charset=UTF-8")
   public String saveScratchAs(@PathParam("NAME") String sName,
                               byte[] data) throws Exception{      
      log.trace(LOG + "SAVE name=" + sName + ", length=" + data.length);

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      main.dialogSaveAs(sName, data);
      
      return "";
   }
}
