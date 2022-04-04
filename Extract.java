import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetTimeFromLoggerTxt{
    
    public static void main(String[]args) throws FileNotFoundException {
        {
        	System.out.println("����Ҫ�������ĵ�:");
        	Scanner sc=new Scanner(System.in);
        	String s=sc.next();
            //ƥ�����
            int matchTime = 0;
            //��ƥ���ϵ��ַ���
            List<String> strs = new ArrayList<>();
            try
            {
                //�����ʽ
                String encoding = "GBK";
                //�ļ�·��
                File file = new File("D:\\Java\\SFile\\"+s);
                if (file.isFile() && file.exists()){ // �ж��ļ��Ƿ����
                    //������
                    InputStreamReader read = new InputStreamReader(
                            new FileInputStream(file), encoding);// ���ǵ������
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    //��ȡһ��
                    while ((lineTxt = bufferedReader.readLine()) != null)
                    {
                        //������ʽ
                         getMatch(matchTime,lineTxt);
                        //System.out.println(matchTime);
                    }
                    read.close();
                }
                else
                {
                    System.out.println("�Ҳ���ָ�����ļ�");
                }
            }
            catch (Exception e)
            {
                System.out.println("��ȡ�ļ����ݳ���");
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