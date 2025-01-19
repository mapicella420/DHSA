package com.group01.dhsa.Model.CDAResources;

import java.util.HashMap;
import java.util.Map;

public class CodiceFiscaleCalculator {
    private static final Map<String, String> comuniCodici = new HashMap<>();
    private static final Map<String, String> paesiCodici = new HashMap<>();

    static {
        // Codici di esempio per i comuni italiani
        comuniCodici.put("ROMA", "H501");
        comuniCodici.put("MILANO", "F205");

        // Codici di esempio per i paesi esteri
        paesiCodici.put("STATI UNITI", "Z404");
        paesiCodici.put("FRANCIA", "Z110");
        paesiCodici.put("GERMANIA", "Z112");
    }

    private String nome;
    private String cognome;
    private int giorno;
    private String mese;
    private int anno;
    private String sesso;
    private String luogo; // Comune o paese di nascita
    private boolean isEstero; // Indica se è un paese estero

    public CodiceFiscaleCalculator(String nome, String cognome, int giorno, String mese, int anno, String sesso, String luogo, boolean isEstero) {
        this.nome = nome.toUpperCase();
        this.cognome = cognome.toUpperCase();
        this.giorno = giorno;
        this.mese = mese;
        this.anno = anno;
        this.sesso = sesso.toUpperCase();
        this.luogo = luogo.toUpperCase();
        this.isEstero = isEstero;
    }

    public String calcolaCodiceFiscale() {
        String codiceCognome = codificaCognome(cognome);
        String codiceNome = codificaNome(nome);
        String codiceData = codificaData(giorno, mese, anno, sesso);
        String codiceLuogo = isEstero ? paesiCodici.getOrDefault(luogo, "Z999") : comuniCodici.getOrDefault(luogo, "XXXX");
        String codiceParziale = codiceCognome + codiceNome + codiceData + codiceLuogo;
        String carattereControllo = calcolaCarattereControllo(codiceParziale);
        return codiceParziale + carattereControllo;
    }

    private String codificaCognome(String cognome) {
        String consonanti = cognome.replaceAll("[AEIOU]", "");
        String vocali = cognome.replaceAll("[^AEIOU]", "");
        return (consonanti + vocali + "XXX").substring(0, 3).toUpperCase();
    }

    private String codificaNome(String nome) {
        String consonanti = nome.replaceAll("[AEIOU]", "");
        String vocali = nome.replaceAll("[^AEIOU]", "");
        if (consonanti.length() > 3) {
            consonanti = consonanti.charAt(0) + consonanti.substring(2, 4);
        }
        return (consonanti + vocali + "XXX").substring(0, 3).toUpperCase();
    }

    private String codificaData(int giorno, String mese, int anno, String sesso) {
        String annoStr = String.valueOf(anno).substring(2);
        String meseStr = meseCodice(mese);
        if (sesso.equals("F")) {
            giorno += 40;
        }
        String giornoStr = (giorno < 10) ? "0" + giorno : String.valueOf(giorno);
        return annoStr + meseStr + giornoStr;
    }

    private String meseCodice(String mese) {
        switch (mese.toUpperCase()) {
            case "GENNAIO": return "A";
            case "FEBBRAIO": return "B";
            case "MARZO": return "C";
            case "APRILE": return "D";
            case "MAGGIO": return "E";
            case "GIUGNO": return "H";
            case "LUGLIO": return "L";
            case "AGOSTO": return "M";
            case "SETTEMBRE": return "P";
            case "OTTOBRE": return "R";
            case "NOVEMBRE": return "S";
            case "DICEMBRE": return "T";
            default: return "X";
        }
    }

    private String calcolaCarattereControllo(String codice) {
        int somma = 0;
        for (int i = 0; i < codice.length(); i++) {
            char c = codice.charAt(i);
            int valore = (i % 2 == 0) ? valoreDispari(c) : valorePari(c);
            somma += valore;
        }
        return String.valueOf((char) ('A' + (somma % 26)));
    }

    private int valoreDispari(char c) {
        switch (c) {
            case '0': return 1;
            case '1': return 0;
            case '2': return 5;
            case '3': return 7;
            case '4': return 9;
            case '5': return 13;
            case '6': return 15;
            case '7': return 17;
            case '8': return 19;
            case '9': return 21;
            case 'A': return 1;
            case 'B': return 0;
            case 'C': return 5;
            case 'D': return 7;
            case 'E': return 9;
            case 'F': return 13;
            case 'G': return 15;
            case 'H': return 17;
            case 'I': return 19;
            case 'J': return 21;
            case 'K': return 1;
            case 'L': return 0;
            case 'M': return 5;
            case 'N': return 7;
            case 'O': return 9;
            case 'P': return 13;
            case 'Q': return 15;
            case 'R': return 17;
            case 'S': return 19;
            case 'T': return 21;
            case 'U': return 1;
            case 'V': return 0;
            case 'W': return 5;
            case 'X': return 7;
            case 'Y': return 9;
            case 'Z': return 13;
            default: return 0;
        }
    }

    private int valorePari(char c) {
        switch (c) {
            case '0': return 0;
            case '1': return 1;
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
            case 'A': return 0;
            case 'B': return 1;
            case 'C': return 2;
            case 'D': return 3;
            case 'E': return 4;
            case 'F': return 5;
            case 'G': return 6;
            case 'H': return 7;
            case 'I': return 8;
            case 'J': return 9;
            case 'K': return 0;
            case 'L': return 1;
            case 'M': return 2;
            case 'N': return 3;
            case 'O': return 4;
            case 'P': return 5;
            case 'Q': return 6;
            case 'R': return 7;
            case 'S': return 8;
            case 'T': return 9;
            case 'U': return 0;
            case 'V': return 1;
            case 'W': return 2;
            case 'X': return 3;
            case 'Y': return 4;
            case 'Z': return 5;
            default: return 0;
        }
    }
}

