/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.utils;

/**
 *
 * @author Dmitry Zubanov (zubanov@gmail.com)
 * date 11.05.2009
 */
public class NumberToWords {

    public static String convert(int n) {
        String words = "";
        String x1[] = {"", "одна", "две", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"};
        String x11[] = {"", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"};
        String x2[] = {"десять", "одинадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестьнадцать", "семьнадцать", "восемьнадцать", "девятнадцать"};
        String x3[] = {"", "", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};
        String x4[] = {"", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот"};


        if (n < 10) {
            words += x1[n];

        } else if (n > 9 && n < 20) {
            int u = n % 10;
            words += " " + x2[u];

        } else if (n >= 20 && n < 100) {
            int u = n / 10;
            int l = n % 10;
            words += " " + x3[u] + " " + x1[l];

        } else if (n > 99 && n < 1000) {
            int u = n / 100;
            int u2 = n - (u * 100);
            words += " " + x4[u];

            if (u2 < 10) {
                words += " " + x11[u2];

            } else if (u2 > 9 && u2 < 20) {
                u = u2 % 10;
                words += " " + x2[u];

            } else if (u2 >= 20 && u2 < 100) {
                u = u2 / 10;
                words += " " + x3[u];

                int u3 = u2 - (u * 10);
                words += " " + x11[u3];
                //System.out.print(" " + x1[u3]);
            }

        } else if (n >= 1000 && n < 99000) {
            int uf = n / 1000;

            if (uf < 10) {
                if (uf == 1) {
                    words += " " + x1[uf] + " тысяча ";
                } else if (uf > 1 && uf < 5) {
                    words += " " + x1[uf] + " тысячи ";
                } else {
                    words += " " + x1[uf] + " тысяч ";
                }

            } else if (uf > 9 && uf < 20) {
                int uff = uf % 10;
                words += " " + x2[uff] + " тысяч ";

            } else if (uf >= 20 && uf < 100) {
                int uff = uf / 10;
                int ufff = uf % 10;

                if (ufff == 1) {
                    words += " " + x3[uff] + " " + x1[ufff] + " тысяча ";
                } else if (ufff > 1 && ufff < 5) {
                    words += " " + x3[uff] + " " + x1[ufff] + " тысячи ";
                } else {
                    words += " " + x3[uff] + " " + x1[ufff] + " тысяч ";
                }

            }

            n = n - (1000 * uf);
            if (n < 10) {
                words += x1[n];

            } else if (n > 9 && n < 20) {
                int u = n % 10;
                words += " " + x2[u];

            } else if (n >= 20 && n < 100) {
                int u = n / 10;
                int l = n % 10;
                words += " " + x3[u] + " " + x1[l];

            } else if (n > 99 && n < 1000) {
                int u = n / 100;
                int u2 = n - (u * 100);
                words += " " + x4[u];

                if (u2 < 10) {
                    words += " " + x1[u2];

                } else if (u2 > 9 && u2 < 20) {
                    u = u2 % 10;
                    words += " " + x2[u];

                } else if (u2 >= 20 && u2 < 100) {
                    u = u2 / 10;
                    words += " " + x3[u];

                    int u3 = u2 - (u * 10);
                    words += " " + x11[u3];
                }
            }
        }

        //System.out.println(words);
        if (words.length() > 2) {
            String first = words.substring(1, 2);
            words = first.toUpperCase() + words.substring(2, words.length());
        }
        
        words = words.replaceAll("тысяч одна", "тысяч один");
        return words;
    }
}
