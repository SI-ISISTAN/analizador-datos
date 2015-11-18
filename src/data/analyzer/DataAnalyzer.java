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
import lotr.LotRGame;
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
    }
  

    public ArrayList<UserSchema> getUsers(){
       return dataInput.getUsers();
    }
    
    public ArrayList<GameSchema> getGamesForUser(String keyAttribute){
       return dataInput.getGamesForUser(keyAttribute);
    }
    
    public ArrayList<GameSchema> getUnanalizedGames(String filter){
        
       ArrayList<GameSchema> allGames = dataInput.getGames();
       ArrayList<GameSchema> unanalyzed = new ArrayList();
       for (GameSchema game: allGames){
           //cast json boolean to string for comparison
           if (filter==null){
                if (!game.isAnalyzed()){
                    unanalyzed.add(game);
                }
           }
           else{
               //si hay filtro por config chequeo que tenga esa config
               LotRGame g = (LotRGame)game;
               if (g.get("configName")!=null){
                   System.out.println("Filter "+filter);
                   System.out.println("Config "+(String)g.get("configName"));
                   if (filter.equals((String)g.get("configName"))){
                       System.out.println("DIO TRUE");
                       if (!game.isAnalyzed()){
                            unanalyzed.add(game);
                        }
                   }
               }
           }
       }
       return unanalyzed;
    }
    
    public void analyzeGames(ArrayList<GameSchema> games){
         for (GameSchema game : games){
            model.evaluateGame(game);
        }
    }
    
    public Model getModel() {
        return model;
    }
   
    
}
