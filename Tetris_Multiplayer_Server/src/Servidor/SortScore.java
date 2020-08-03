package Servidor;

import java.util.Comparator;

class SortScore implements Comparator<DataPlayer> 
{ 
    public int compare(DataPlayer a, DataPlayer b) 
    { 
        return b.getScore()-a.getScore(); 
    } 
} 
