package com.volantetech.volante.services.identitymanager;

import com.tplus.transform.runtime.*;
import com.volantetech.services.engine.LookUpUtils;
import com.volantetech.services.engine.plugin.registry.IDGeneratorFunctions;

import java.rmi.RemoteException;

/**
 * Created by Monikandan on 27/9/21.
 */

@IDGeneratorFunctions(functionPrefix = "BusinessDateFunction")
public class IDGeneratorBusinessDate {
    private static MessageFlow businessDateFlow;

    public static MessageFlow businessDateFlow() throws TransformException {
        // look up message flow and cache
        if(businessDateFlow == null) {
            // Lookup message flow (defined in the cartridge)
            businessDateFlow = LookUpUtils.getMessageFlow("GetBusinessDateFlow");
        }
        return businessDateFlow;
    }

    /**
     * @param TenantId tenant-ID for an entity
     * @return businessDate from reference data entity
     */
    public static String getBusinessDate(String TenantId) {
        String businessDate="";
        TransformContext transformContext = new TransformContextImpl();
        try {
            Object[] output = businessDateFlow().run(new Object[]{TenantId}, transformContext);
            businessDate=(String)output[0];
        }
        catch(RemoteException | TransformException e) {
            throw  new TransformRuntimeException(e.getMessage());
        }
        return businessDate;
    }
}

