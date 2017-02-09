/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Statistics;

/**
 *
 * @author kostas
 */
public class NetRateStats {
      
    String _interface;
    String _timeStamp;
    double _Kbps_in;
    double _Kbps_out;

    
    public String getInterface() {
        return _interface;
    }

    public String getTimeStamp() {
        return _timeStamp;
    }

    public void setTimeStamp(String timestamp) {
        this._timeStamp = timestamp;
    }

    public double getKbps_in() {
        return _Kbps_in;
    }

    public void setKbps_in(double Kbps_in) {
        this._Kbps_in = Kbps_in;
    }

    public double getKbps_out() {
        return _Kbps_out;
    }

    public void setKbps_out(double Kbps_out) {
        this._Kbps_out = Kbps_out;
    }

    public void setInterface(String _interface) {
        this._interface = _interface;
    }

    
    
    
}
