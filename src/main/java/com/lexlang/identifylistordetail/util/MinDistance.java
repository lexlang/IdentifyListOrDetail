package com.lexlang.identifylistordetail.util;


public class MinDistance {

    public static int minDistance(String word1, String word2) {
        //dp[i][j]表示源串A位置i到目标串B位置j处最低需要操作的次数
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];
        for(int i = 0; i< word1.length() + 1; i++){
            dp[i][0] = i;
        }
        for(int j = 0; j< word2.length() + 1; j++){
            dp[0][j] = j;
        }
        for(int i = 1; i< word1.length() + 1; i++){
            for(int j = 1; j< word2.length() + 1; j++){
                if(word1.charAt(i - 1) == word2.charAt(j - 1)){  // 第i个字符是字符串下标为i-1第哪个
                    dp[i][j] = dp[i - 1][j - 1];
                }else{
                    dp[i][j] = (Math.min(Math.min(dp[i-1][j], dp[i][j-1]), dp[i-1][j-1])) + 1;
                }
            }
        }
        return dp[word1.length()][word2.length()];
    }
    
    public static double distanceSimilarity(String word1, String word2){
    	int coun=word1.length()+word2.length();
    	return (coun-minDistance(word1,word2))*1.0/coun;
    }
    
    public static void main(String[] args) {
        String test1 = "body/div[14]/div[1]/div[8]/div[2]/ul[0]/li[5]/div[0]/h4[0]/a";
        String test2 = "body/div[14]/div[1]/div[8]/div[2]/ul[0]/li[10]/div[0]/h4[0]/a";
        int result = minDistance(test1, test2);
        System.out.println(result);
    }

}
