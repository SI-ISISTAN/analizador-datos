/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.analyzer;

/**
 *
 * @author matias
 */
public interface ConnectionData {
    public abstract void connect(DataInput input) throws InvalidInputException;
}
