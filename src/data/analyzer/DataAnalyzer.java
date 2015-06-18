/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.analyzer;

//Parser de Json
import org.json.simple.JSONObject;  

import java.net.UnknownHostException;
import java.util.ArrayList;
import connection.lotr.*;
/**
 *
 * @author matias
 */
public class DataAnalyzer {
    
    private DataInput dataInput;
    private Model model;
    
    public DataAnalyzer(DataInput data, Model m) throws UnknownHostException{
       model=m;
       dataInput = data;
       dataInput.connectToSource();
    }
  

    public ArrayList<UserSchema> getUsers(){
       return dataInput.getUsers();
    }
    
    public ArrayList<GameSchema> getGamesForUser(String keyAttribute){
       return dataInput.getGamesForUser(keyAttribute);
    }

    public Model getModel() {
        return model;
    }
   
    
}