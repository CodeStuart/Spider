import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetTimeFromLoggerTxt{
    
    public static void main(String[]args) throws FileNotFoundException {
        {
        	System.out.println("输入要检索的文档:");
        	Scanner sc=new Scanner(System.in);
        	String s=sc.next();
            //匹配次数
            int matchTime = 0;
            //存匹配上的字符串
            List<String> strs = new ArrayList<>();
            try
            {
                //编码格式
                String encoding = "GBK";
                //文件路径
                File file = new File("D:\\Java\\SFile\\"+s);
                if (file.isFile() && file.exists()){ // 判断文件是否存在
                    //输入流
                    InputStreamReader read = new InputStreamReader(
                            new FileInputStream(file), encoding);// 考虑到编码格
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    //读取一行
                    while ((lineTxt = bufferedReader.readLine()) != null)
                    {
                        //正则表达式
                         getMatch(matchTime,lineTxt);
                        //System.out.println(matchTime);
                    }
                    read.close();
                }
                else
                {
                    System.out.println("找不到指定的文件");
                }
            }
            catch (Exception e)
            {
                System.out.println("读取文件内容出错");
                e.printStackTrace();
            }
        }
    }
    private static void getMatch(int matchTime,String lineTxt) {
        Pattern p = Pattern.compile("(?<=<td>).*(?=</td>)");
        Matcher m = p.matcher(lineTxt);
        boolean result = m.find();
        String find_result = null;
        if (result)
        {
            matchTime++;
            find_result = m.group(0);
            System.out.println(find_result);
        }else{
        	//System.out.println("not find");
        }
    }
}