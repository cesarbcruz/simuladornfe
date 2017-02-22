/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cesar.com.simulador;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author cesar
 */
public class Executavel {

    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(new File(System.getProperty("user.dir") + "/SimuladorNFe.properties")));
        new SimuladorNfe(prop).iniciarProcesso();
    }
}
