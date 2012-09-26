/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.pos;

import java.nio.file.Path;
import org.ubo.utils.Result;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface FiscalPrinter {
    
    public Result printZReport(Path zReportFile);
    
    public Result printXReport(Path zReportFile);
    
    public Result printReceipt(Path receiptsDir);
    
    public Result printReceiptMoneyDeposition(Path receiptsFile);
    
    public Result printReceiptMoneyOut(Path receiptsFile);
    
}
