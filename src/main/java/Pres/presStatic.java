package Pres;


import Dao.DaoImpl;
import Dao.IDao;
import Metier.MetierImpl;

public class Stat {
    public static void main(String args[]) {
        IDao d = new DaoImpl();
        MetierImpl metier = new MetierImpl(d);
        //metier.setDao(d);
        System.out.println("res="+metier.calculate());
    }
}
