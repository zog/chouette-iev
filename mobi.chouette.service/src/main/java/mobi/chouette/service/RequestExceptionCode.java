/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobi.chouette.service;

/**
 *
 * @author marc
 */
public enum RequestExceptionCode {
    
    UNKNOWN_REFERENTIAL, 
    UNKNOWN_FILE, 
    UNKNOWN_ACTION, 
    ACTION_TYPE_MISMATCH, 
    DUPPLICATE_OR_MISSING_DATA, 
    DUPPLICATE_PARAMETERS, 
    MISSING_PARAMETERS, 
    INVALID_PARAMETERS, 
    UNREADABLE_PARAMETERS,
    UNKNOWN_JOB, 
    SCHEDULED_JOB
}
