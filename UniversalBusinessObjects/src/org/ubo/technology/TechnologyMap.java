/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.technology;

import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;

/**
 *
 * @author anton
 */
public class TechnologyMap implements KnowsId, Serializable {

    static final long serialVersionUID = 1L;
    
    private long id;
    
    private long idCrew = -1;

    public long getIdCrew() {
        return idCrew;
    }

    public void setIdCrew(long idCrew) {
        this.idCrew = idCrew;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }
    
}
