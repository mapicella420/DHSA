package com.group01.dhsa.Model.CDAResources;

import java.util.HashMap;
import java.util.Map;

/**
 * The `CodiceFiscaleCalculator` class calculates the Italian fiscal code
 * (Codice Fiscale) based on personal information such as name, surname, birth
 * date, gender, and place of birth. The fiscal code is an alphanumeric string
 * that uniquely identifies individuals in Italy.
 */
public class CodiceFiscaleCalculator {

    // Static maps for storing codes for Italian municipalities and foreign countries
    private static final Map<String, String> comuniCodici = new HashMap<>();
    private static final Map<String, String> paesiCodici = new HashMap<>();

    // Static initializer for populating example codes
    static {
        comuniCodici.put("ROMA", "H501"); // Example for Rome
        comuniCodici.put("MILANO", "F205"); // Example for Milan

        paesiCodici.put("STATI UNITI", "Z404"); // Example for the USA
        paesiCodici.put("FRANCIA", "Z110");    // Example for France
        paesiCodici.put("GERMANIA", "Z112");   // Example for Germany
    }

    // Instance variables for storing personal details
    private String nome;
    private String cognome;
    private int giorno;
    private String mese;
    private int anno;
    private String sesso;
    private String luogo; // Municipality or country of birth
    private boolean isEstero; // True if place of birth is a foreign country

    /**
     * Constructor to initialize the calculator with personal details.
     *
     * @param nome     First name
     * @param cognome  Last name
     * @param giorno   Day of birth
     * @param mese     Month of birth
     * @param anno     Year of birth
     * @param sesso    Gender ("M" for male, "F" for female)
     * @param luogo    Place of birth (municipality or country)
     * @param isEstero True if the place of birth is a foreign country
     */
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

    /**
     * Calculates the fiscal code based on the provided details.
     *
     * @return The calculated fiscal code as a string
     */
    public String calcolaCodiceFiscale() {
        String codiceCognome = codificaCognome(cognome);
        String codiceNome = codificaNome(nome);
        String codiceData = codificaData(giorno, mese, anno, sesso);
        String codiceLuogo = isEstero ? paesiCodici.getOrDefault(luogo, "Z999") : comuniCodici.getOrDefault(luogo, "XXXX");
        String codiceParziale = codiceCognome + codiceNome + codiceData + codiceLuogo;
        String carattereControllo = calcolaCarattereControllo(codiceParziale);
        return codiceParziale + carattereControllo;
    }

    // Encodes the surname into its fiscal code format
    private String codificaCognome(String cognome) {
        String consonanti = cognome.replaceAll("[AEIOU]", "");
        String vocali = cognome.replaceAll("[^AEIOU]", "");
        return (consonanti + vocali + "XXX").substring(0, 3).toUpperCase();
    }

    // Encodes the first name into its fiscal code format
    private String codificaNome(String nome) {
        String consonanti = nome.replaceAll("[AEIOU]", "");
        String vocali = nome.replaceAll("[^AEIOU]", "");
        if (consonanti.length() > 3) {
            consonanti = consonanti.charAt(0) + consonanti.substring(2, 4);
        }
        return (consonanti + vocali + "XXX").substring(0, 3).toUpperCase();
    }

    // Encodes the birth date and gender into its fiscal code format
    private String codificaData(int giorno, String mese, int anno, String sesso) {
        String annoStr = String.valueOf(anno).substring(2);
        String meseStr = meseCodice(mese);
        if (sesso.equals("F")) {
            giorno += 40; // Adds 40 to the day if the gender is female
        }
        String giornoStr = (giorno < 10) ? "0" + giorno : String.valueOf(giorno);
        return annoStr + meseStr + giornoStr;
    }

    // Maps the month into its corresponding fiscal code letter
    private String meseCodice(String mese) {
        switch (mese.toUpperCase()) {
            case "01": return "A";
            case "02": return "B";
            case "03": return "C";
            case "04": return "D";
            case "05": return "E";
            case "06": return "H";
            case "07": return "L";
            case "08": return "M";
            case "09": return "P";
            case "10": return "R";
            case "11": return "S";
            case "12": return "T";
            default: return "X"; // Default for invalid months
        }
    }

    // Calculates the control character for the fiscal code
    private String calcolaCarattereControllo(String codice) {
        int somma = 0;
        for (int i = 0; i < codice.length(); i++) {
            char c = codice.charAt(i);
            int valore = (i % 2 == 0) ? valoreDispari(c) : valorePari(c); // Odd and even positions
            somma += valore;
        }
        return String.valueOf((char) ('A' + (somma % 26))); // Maps sum to a letter
    }

    // Calculates the value for odd-positioned characters
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
            // Continue for all characters...
            default: return 0;
        }
    }

    // Calculates the value for even-positioned characters
    private int valorePari(char c) {
        switch (c) {
            case '0': return 0;
            case '1': return 1;
            case '2': return 2;
            case '3': return 3;
            // Continue for all characters...
            default: return 0;
        }
    }
}
