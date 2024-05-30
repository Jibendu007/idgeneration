package com.volantetech.volante.services.identitymanager;

import com.tplus.transform.runtime.DataObject;
import com.tplus.transform.runtime.TransformContext;
import com.tplus.transform.runtime.TransformContextImpl;
import com.tplus.transform.runtime.TransformRuntimeException;
import com.tplus.transform.runtime.collection.StringList;
import com.tplus.transform.util.log.Log;
import com.volantetech.services.engine.identitymanager.IDManagerHelper;
import com.volantetech.services.engine.util.IdGeneratorThreadLocal;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;

import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.*;

import static com.volantetech.services.engine.log.LoggingTemplate.transformToJsonLogger;

/**
 * Created by Sivaranjani on 10/5/18.
 */
public class IdGenerator {
    private static Log log = com.tplus.transform.util.log.LogFactory.getLog(IdGenerator.class);
    private static final String TENANT_ID = "TenantId";
    private static final String AND_ID_CD = " and IdCd ";
    private static final String ERROR_MESSAGE = "Error in ID generation for the IDConfiguration having TenantId {0}" + AND_ID_CD + "{1}. ";

    /**
     * to load all the functions in registry for first time
     */

    private IdGenerator() {
    }



    static {
        loadIdManagerFunction();
    }

    /**
     * method to generate unique identifier based on configured pattern
     * Usuage :
     * pattern : BNYM-${SYSDATE(YYYYMMDD)}-${n}
     * template: BNYM-$function.currentDate("yyyyMMdd")-$function.getSequenceKey(3,$tenantID,$seqGenSeqID)
     * value for $tenantId and $seqGenSeqID should be passed in map
     *
     * @param idObj         -  IDConfiguration object for corresponding IDCode
     * @param macroAndValue - macro values
     * @return Identifier
     * @throws IdManagerException
     */
    public static String getIdentifer(DataObject idObj, com.tplus.transform.runtime.cache.Map macroAndValue) throws IdManagerException {
        String template = (String) idObj.getField("Templt");
        String idCode = (String) idObj.getField("IdCd");

        return (String) generateIdentifier(idObj, template, idCode, macroAndValue, 1).get(0);
    }

    public static StringList getIdentifers(DataObject idObj, com.tplus.transform.runtime.cache.Map macroAndValue, int count) throws IdManagerException {
        String template = (String) idObj.getField("Templt");
        String idCode = (String) idObj.getField("IdCd");
        return generateIdentifier(idObj, template, idCode, macroAndValue, count);

    }

    /**
     * method to generate identifier based on velocity template
     *
     * @param template      - velocity template
     * @param macroAndValue - macro values
     * @return generated Identifier
     * {@link IdentityManagerFunctions} to invoke functions
     */


    static StringList generateIdentifier(DataObject idObj, String template, String idcode, com.tplus.transform.runtime.cache.Map macroAndValue, int count) throws IdManagerException {

        StringList idList = new StringList(count);

        com.tplus.transform.runtime.cache.MapImpl map = (com.tplus.transform.runtime.cache.MapImpl) macroAndValue;

        VelocityContext velocityContext = new VelocityContext();

        /*VB-5786 Thread Local for IDGenration Logic with SEQID and Date*/
        if(template.contains("getSequenceKeyWithDate") || template.contains("getKeyByEntityNameWithDate")) {
            String date =template.contains("getSequenceKeyWithDate") ? template.split(",")[3] : template.split(",")[4];
            date=date.replaceAll("[$)]", "");
            if (IdGeneratorThreadLocal.getThreadLocalIdGenerate().size()>0) {
                HashMap<String, String> threadlocalmap = (HashMap<String, String>) IdGeneratorThreadLocal.getThreadLocalIdGenerate().get(0);
                if(threadlocalmap.get(date)!=null){
                velocityContext.put(date, threadlocalmap.get(date));
                }
            }
        }

        for (String key : (Iterable<String>) map.keySet()) {
            velocityContext.put(key, macroAndValue.get(key));

        }
        IDManagerHelper instance = IDManagerHelper.getInstance();
        for (Map.Entry<String, Object> stringObjectEntry : instance.getFunctionRegistry().entrySet()) {

            velocityContext.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());

        }


        boolean skipValidation = false;
        try {

            for (int i = 0; i < count; i++) {
                StringWriter out = new StringWriter();
                Velocity.evaluate(velocityContext, out, "Id generation", template);
                String id = String.valueOf(out);
                if (!skipValidation) {
                    validateIdLength(idObj, id, idcode);
                    skipValidation = true;
                }
                idList.add(id);
            }

        } catch (ParseErrorException e) {
            throw new IdManagerException(MessageFormat.format(ERROR_MESSAGE, idObj.getField(TENANT_ID), idcode) + " Exception while parsing template with IDCode : " + idcode + ". " + e.getMessage());
        } catch (MethodInvocationException e) {
            Throwable wrappedThrowable = e.getCause();
            if (wrappedThrowable instanceof IdManagerException) {
                throw ((IdManagerException) wrappedThrowable);
            }
            throw new IdManagerException(MessageFormat.format(ERROR_MESSAGE, idObj.getField(TENANT_ID), idcode) + e.getMessage(), e);

        } catch (IdManagerException e) {
            e.setMessage(MessageFormat.format(ERROR_MESSAGE, idObj.getField(TENANT_ID), idcode) + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new IdManagerException(MessageFormat.format(ERROR_MESSAGE, idObj.getField(TENANT_ID), idcode) + e.getMessage(), e);
        }
        return idList;
    }

    /**
     * method to validate length of generated Identifier (Fixed / min and max)
     *
     * @param idObj IDConfiguration object for corresponding IDCode
     * @param id    Generated Identifier
     * @throws IdManagerException
     */

    private static void validateIdLength(DataObject idObj, String id, String idCode) throws IdManagerException {

        Object fixLen = idObj.getField("FxdLen");
        int generatedIdLength = id.length();

        if (fixLen != null) {
            if ((int) fixLen != generatedIdLength) {
                throw new IdManagerException("The length of the generated identifier does not match with the configured fixed length for the TenantId " + idObj.getField(TENANT_ID) + AND_ID_CD + idCode + "; Generated ID length: " + generatedIdLength + ", expected length " + fixLen);

            }
        } else {
            int minLen = (int) idObj.getField("MinLen");
            int maxLen = (int) idObj.getField("MaxLen");
            if (!(generatedIdLength >= minLen && generatedIdLength <= maxLen)) {
                throw new IdManagerException("The length of the generated identifier does not fall in the range of the configured min and max length for the TenantId " + idObj.getField(TENANT_ID) + AND_ID_CD + idCode + "; Generated ID length: " + generatedIdLength + ", expected range " + minLen + " <= length <= " + maxLen);
            }

        }
    }

    /**
     * Method to load all function files and add to registery
     */
    private static synchronized void loadIdManagerFunction() {
        TransformContext transformContext = new TransformContextImpl();
        if (log.isDebugEnabled())
            log.debug(transformToJsonLogger("Loading Idmanager functions", transformContext));
        try {
            if (log.isDebugEnabled())
                log.debug(transformToJsonLogger("Initialize velocity template", transformContext));
            Velocity.setProperty(org.apache.velocity.runtime.RuntimeConstants.RESOURCE_LOADER, "classpath");
            Velocity.setProperty("classpath.resource.loader.class", org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader.class.getName());
            Velocity.setProperty("velocimacro.library", "VM_global_library.vm");
            Velocity.setProperty("velocimacro.arguments.strict", true);
            Velocity.setProperty("runtime.references.strict", true);
            Velocity.init();
            if (log.isDebugEnabled())
                log.debug(transformToJsonLogger("Velocity initialization is compelted", transformContext));
        } catch (Exception e) {
            throw new TransformRuntimeException("Exception while loading Identity manager functions. "+e.getMessage(), e);
        }

    }

}
