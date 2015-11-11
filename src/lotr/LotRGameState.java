/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lotr;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import lotr.LotRGame;
import lotr.LotRGameAction;
import java.util.Hashtable;
import org.json.simple.JSONObject;

/**
 *
 * @author matias
 */
public class LotRGameState {
    int sauronPosition;
    int locationNumber;
    int locationsAmount;
    int an;
    
    Hashtable<String, LotRPlayerState> players;
    DBObject config;
    
    public LotRGameState(){
        sauronPosition=0;
        locationNumber=0;
        locationsAmount=0;
        an=0;
        players = new Hashtable<String, LotRPlayerState>();
        config=null;
    }
    
    public void addPlayer(String alias){
        players.put(alias, new LotRPlayerState());
    }
    
    public void loadInitialState(DBObject config){
       this.config=config;
       sauronPosition = (int)config.get("sauronPosition");
       locationsAmount = ((BasicDBList)(config.get("locations"))).size();
       
       //cargar los valores de cada variable para los play5r
       if (config.get("shieldTokens")!=null){
           for (String key : players.keySet()){
               players.get(key).setShields((int)config.get("shieldTokens"));
           }
       }
       if (config.get("lifeTokens")!=null){
           for (String key : players.keySet()){
               players.get(key).setLifeTokens((int)config.get("lifeTokens"));
           }
       }
       if (config.get("ringTokens")!=null){
           for (String key : players.keySet()){
               players.get(key).setRingTokens((int)config.get("ringTokens"));
           }
       }
       if (config.get("sunTokens")!=null){
           for (String key : players.keySet()){
               players.get(key).setSunTokens((int)config.get("sunTokens"));
           }
       }
    }

    @Override
    public String toString() {
        String ret = "LotRGameState{" + "sauronPosition=" + sauronPosition + ", locationNumber=" + locationNumber + ", locationsAmount=" + locationsAmount + ", players: \n" ;
        for (String key : players.keySet()){
                    ret+=key+players.get(key).toString();
        }
        return ret;
    }
    
    //Tienen un par de acciones básicas
    public void update (LotRGameAction action){
        String name = (String)action.get("action");
        DBObject data = (DBObject)action.get("data");
        //habra que hacer seguimiento de turnos?
        switch(name){
            
            case "DealHobbitCards":
                if (data.get("player")==null){
                    for (String key : players.keySet()){
                        players.get(key).addCards((int)data.get("amount"));
                    }
                }
                else{
                    players.get((String)data.get("player")).addCards((int)data.get("amount"));
                }
                break;
            case "DealFeatureCards":
                BasicDBList given = (BasicDBList)data.get("given");
                for (Object o : given){
                    DBObject card = (DBObject)o;
                    players.get((String)card.get("player")).addCards(1);
                }
                break;
            case "PlayerDiscard":
                //ir carta por carta y no contar las especiales
                //esto es cuanto menos, polémico
                BasicDBList discards = (BasicDBList)data.get("discard");
                int discarded = discards.size();
                players.get((String)data.get("player")).addCards(-discarded);
                break;
            case "MovePlayer":
                players.get((String)data.get("alias")).move((int)data.get("amount"));
                break;
            case "MoveSauron":
                this.sauronPosition-=(int)data.get("amount");
                break;    
            case "PlayerGiveCards":
                int amount =((BasicDBList)data.get("cards")).size();
                players.get((String)data.get("to")).addCards(amount);
                players.get((String)data.get("from")).addCards(-amount);
                break;      
            case "ChangeTokens":
                if (data.get("token")!=null){
                    String token = (String)data.get("token");
                    players.get((String)data.get("alias")).changeToken(token, (int)data.get("amount"));
                }
                break; 
            case "PlayCard":
                if (data.get("played")!=null){             
                    DBObject card = (DBObject)data.get("played");
                    if (card.get("type")!=null){
                            players.get((String)data.get("alias")).addCards(-1);
                    }
                }
                break;
            case "PlaySpecialCard":
                players.get((String)action.get("player")).addCards(-1);
                break;
            case "DealFeatureCardByName":
                players.get((String)data.get("player")).addCards(1);
                break;
        }
        an++;
    }
    
    //valores potencialmente configurables
    public boolean getCondition(JSONObject conditionObj, String player){
        boolean ret = false;
        String condition = (String)conditionObj.get("condition");
            if (condition.equals("HasMoreCards")){
                
                int am = players.get(player).getCardsAmount();
                int avg=0;
                for (String key : players.keySet()){
                        avg+=players.get(key).getCardsAmount();
                }
                avg=avg/players.size();
                if (am>avg){
                    ret=true;
                }
            }
            else if (condition.equals("HasMuchMoreCards")){
                int am = players.get(player).getCardsAmount();
                int avg=0;
                for (String key : players.keySet()){
                        avg+=players.get(key).getCardsAmount();
                }
                avg=avg/players.size();
                if (am>avg*1.5){
                    ret=true;
                }

            }
            else if (condition.equals("HasFewerCards")){
                int am = players.get(player).getCardsAmount();
                int avg=0;
                for (String key : players.keySet()){
                        avg+=players.get(key).getCardsAmount();
                }
                avg=avg/players.size();
                if (am<avg){
                    ret=true;
                }
            }
            else if (condition.equals("HasMuchFewerCards")){
                int am = players.get(player).getCardsAmount();
                int avg=0;
                for (String key : players.keySet()){
                        avg+=players.get(key).getCardsAmount();
                }
                avg=avg/players.size();
                if (am*1.5<avg){
                    ret=true;
                }
            }
            else if (condition.equals("HasLessThanCards")){
                long cards = (long)conditionObj.get("amount");
                int am = players.get(player).getCardsAmount();
                if ((long)am<cards){
                    ret=true;
                }
            }
            else if (condition.equals("HasMoreThanCards")){
                long cards = (long)conditionObj.get("amount");
                int am = players.get(player).getCardsAmount();
                if ((long)am>cards){
                    ret=true;
                }
            }
            else if (condition.equals("IsCloseToSauron")){
                int pos = players.get(player).getPosition();
                int sauron = this.sauronPosition;
                if (sauronPosition-pos <= 2){
                    ret=true;
                }
            }
            else if (condition.equals("IsCloserThan")){
                long am = (long)conditionObj.get("amount");
                int pos = players.get(player).getPosition();
                int sauron = this.sauronPosition;
                if (sauronPosition-pos <= am){
                    ret=true;
                }
            }
            else if (condition.equals("IsFurtherThan")){
                long am = (long)conditionObj.get("amount");
                int pos = players.get(player).getPosition();
                int sauron = this.sauronPosition;
                if (sauronPosition-pos >= am){
                    ret=true;
                }
            }
            else if (condition.equals("IsCloserToSauron")){
                int pos = players.get(player).getPosition();
                int avg=0;
                for (String key : players.keySet()){
                        avg+=players.get(key).getPosition();
                }
                avg=avg/players.size();
                if (pos>avg){
                    ret=true;
                }
            }
            else if (condition.equals("IsFurther")){
                int pos = players.get(player).getPosition();
                int avg=0;
                for (String key : players.keySet()){
                        avg+=players.get(key).getPosition();
                }
                avg=avg/players.size();
                if (pos<avg){
                    ret=true;
                }
            }
            else if (condition.equals("HasFewerSunTokens")){
                int tokens = players.get(player).getSunTokens();
                int avg=0;
                for (String key : players.keySet()){
                        avg+=players.get(key).getSunTokens();
                }
                avg=avg/players.size();
                if (tokens<avg){
                    ret=true;
                }
            }
            else if (condition.equals("HasFewerLifeTokens")){
                int tokens = players.get(player).getLifeTokens();
                int avg=0;
                for (String key : players.keySet()){
                        avg+=players.get(key).getLifeTokens();
                }
                avg=avg/players.size();
                if (tokens<avg){
                    ret=true;
                }
            }
            else if (condition.equals("HasFewerRingTokens")){
                int tokens = players.get(player).getRingTokens();
                int avg=0;
                for (String key : players.keySet()){
                        avg+=players.get(key).getRingTokens();
                }
                avg=avg/players.size();
                if (tokens<avg){
                    ret=true;
                }
            }
            else if (condition.equals("HasFewerShieldTokens")){
                int tokens = players.get(player).getShields();
                int avg=0;
                for (String key : players.keySet()){
                        avg+=players.get(key).getShields();
                }
                avg=avg/players.size();
                if (tokens<avg){
                    ret=true;
                }
            }
            else if (condition.equals("HasEqualOrMoreTokens")){
                String token = (String)conditionObj.get("token");
                long am = (long)conditionObj.get("amount");
                if ((long)players.get(player).getToken(token) >= am){
                    ret=true;
                }
            }
            else if (condition.equals("HasEqualOrLessTokens")){
                String token = (String)conditionObj.get("token");
                long am = (long)conditionObj.get("amount");
                if ((long)players.get(player).getToken(token) <= am){
                    ret=true;
                }
            }
        
        return ret;
    }
    
}
