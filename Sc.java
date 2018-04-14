import java.sql.ResultSet;    
import java.sql.SQLException;
import java.net.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;
//import myutil.DBHelper;
import java.net.URL;
import java.net.HttpURLConnection;

import util.Mysql;
class Sc extends Thread
{
	Socket socket=null;
	static BlockingQueue<String> queue=new  LinkedBlockingQueue<String>();//��ʼip��
	static BlockingQueue<String> open=new LinkedBlockingQueue<String>();//��Ծip
	static BlockingQueue<String> rest=new LinkedBlockingQueue<String>();//ɨ��һ�η��ֵĲ���Ծip������ɨ��һ��
	static BlockingQueue<String> close=new LinkedBlockingQueue<String>();//���ζ�����Ծ����Ϊ����ip
	static BlockingQueue<String> mysqlip=new LinkedBlockingQueue<String>();//���ݿ���ip
	static ArrayList<String> listip=new ArrayList<String>();
	Mysql db1=null;
	BufferedReader reader;
	static ResultSet ret = null;
	PrintWriter writer;
	static int num=0;
	static int html=0;
	String str;
	int load=50000;
	int count=1;
	Sc(){
		try
		{
			socket=new Socket("127.0.0.1",9999);
			reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer=new PrintWriter(socket.getOutputStream());
			writer.println("ĸ֩��һ���߳����ӳɹ�!");
			writer.flush();
			String str=reader.readLine();
			num=Integer.parseInt(str); 	
			System.out.println("���ӷ������ɹ�!");
			go();
		}
		catch (Exception e)
		{
		}
	}
	public String toString(){
		return Thread.currentThread()+""; 
	}
	public void init(){
		String ip="";
		synchronized(this){ 
			int dex=num*load;
			while(dex<load*num+load){//514022625
				int a=dex/(255*255*255);
				int b=(dex%(255*255*255))/(255*255);
				int c=(dex%(255*255))/255;
				int d=dex%255;
				ip=(192+a)+"."+(168+b)+"."+c+"."+d;
				queue.add(ip);
				dex++;
			}
			System.out.println(ip);
			ip=null;
		}
	}
	class MomSpider implements Runnable
	{
		BlockingQueue<String> Q,C;
		CountDownLatch ch;
		int flag;
		MomSpider(BlockingQueue<String> qe,BlockingQueue<String> q,CountDownLatch c,int flag){this.flag=flag;this.Q=qe;this.C=q;this.ch=c;}
		ExecutorService exec=Executors.newFixedThreadPool(200);
		private  CyclicBarrier barrier=new CyclicBarrier(200,new Runnable(){
			public void run(){
				if(Q.size()==0){exec.shutdownNow();}
			}
		});
		public void run(){
			try
			{
				for(int i=0;i<200;i++){
					exec.execute(new SonSpider(barrier,Q,C,flag));
					}
					while(true){
						if(Q.size()==0)break;
					}
					exec.shutdownNow();
					ch.countDown();
					
			}
			catch (Exception e)
			{
			}
		}
	}
	class SonSpider implements Runnable
	{
		BufferedReader br;
		FileWriter fw;
		private CyclicBarrier b;
		BlockingQueue<String> Q,C;
		int flag;
		SonSpider(CyclicBarrier b,BlockingQueue<String> qe,BlockingQueue<String> q,int flag){this.flag=flag;this.b=b;Q=qe;this.C=q;}
		public void getCont(){
			//ץȡ��ҳ������
				String sb = mysqlip.poll();
				if(sb==null||sb==""){
						return;
					}
				try {
					URL url = new URL("http://"+sb);
					br = new BufferedReader(new InputStreamReader(url
							.openStream(),"utf-8"));
					fw = new FileWriter(new File("D:\\Java\\SFile\\"+(html++)+".txt"));
					String line;
					while ((line = br.readLine()) != null) {
						fw.write(line);
						fw.flush();
					}
					sb=null;
				} catch (Exception e) { // Report any errors that arise
					//System.out.println(sb);
					System.out.println(e.toString());
					//System.err.println("Usage:java outfile error");
				}
			
		}
		public void saomiao(){
			try
			{
				while(!Thread.interrupted()){
					Socket socket=null;
					for(int i=0;i<10;i++){
						synchronized(this){
						if(Q.size()==0){Thread.currentThread().interrupt();break;}
							String ip=Q.poll();
							try
							{

								InetSocketAddress isa=new InetSocketAddress(ip,80);
								socket=new Socket();
								socket.connect(isa,50);
								open.add(ip);
								listip.add(ip);
								System.out.println(ip+"����80�˿�");
								socket.close();
							}
								catch (Exception e)
								{
									C.add(ip);
								}
								finally{
									try
									{
										if(socket!=null)
										socket.close();	
									}
									catch (Exception ex)
									{
									System.out.println(ex.toString());
									}
								}
						}//for
					}//sy
					
				}
			}
			catch (Exception e)
			{
			}
			
		}
		public void run(){
			if(flag==0)saomiao();
			else getCont();
			try
			{
				b.await();
			}
			catch (Exception wq)
			{
				
			}
			 
		}
	}
	public synchronized void addQueue(){
		insert();
	}
	public synchronized void deleteQueue(BlockingQueue<String> x) {
			String s;
			for(int i=0;i<close.size();i++)
			{
				s=x.poll();
			}
			s=null;
	}
	public  void go(){
		int jishu=0;
		while(true){
			init();
			long st=System.currentTimeMillis();
			CountDownLatch ch=new CountDownLatch(1);
			Thread ms=new Thread(new MomSpider(queue,rest,ch,0));
			try
			{
				ms.start();
				ch.await();	
			}
			catch (Exception e)
			{
				System.out.println(e.toString()+"ch1");
			}
			int a=open.size();
			int b=rest.size();
			//insert();
			long e1t=System.currentTimeMillis();
			System.out.println((e1t-st)+"ms");
			System.out.println("��Ծ��ip:"+a+"��");
			System.out.println("���е�ip:"+b+"��");
			System.out.println("���мӻ�Ծip:"+(a+b)+"��");
			System.out.println("��ip"+(load*(jishu+1))+"��");
			System.out.println("��ʼ�ڶ���ɨ��...");
			ExecutorService ec=Executors.newCachedThreadPool();
			long t1=System.currentTimeMillis();
			CountDownLatch ch2=new CountDownLatch(1);
			Thread ms2=new Thread(new MomSpider(rest,close,ch2,0));
			try
			{
				ms2.start();
				ch2.await();
			}
			catch (Exception e)
			{
				System.out.println(e.toString()+"ch2");
			}
			int a1=listip.size();
			b=close.size();
			long t2=System.currentTimeMillis();
			System.out.println("�ڶ���ɨ����");
			System.out.println("��Ծip��:"+open.size()+"��");
			System.out.println("�·��ֵĻ�Ծ��ip:"+(open.size()-a)+"��");
			System.out.println("����Ծ����:"+b+"��");
			System.out.println((t2-t1)+"ms");
			str="";
			try
			{	
				String sss;
				sss="ĸ֩��һ:ɨ���߳����!ɨ����"+(jishu+1)*load+"��ip";
				if(open.size()>0){
					ms.stop();
					ms=null;
					ms2.stop();
					ms2=null;
					addQueue();
					if(a1>0)
					sss="ĸ֩��һ:ɨ���߳����!\nĸ֩��һ:�洢����......�洢��"+a1+"��ip";
					else 
					sss="ĸ֩��һ:ɨ���߳����!";
					}
				if(close.size()>200000){
					deleteQueue(close);
					sss+="ɾ����������......ɾ����"+(close.size())+"��ip";
					}		
				writer.println(sss);
				writer.flush();
				while(true){
					str=reader.readLine();
					if(str!=""||str!=null){
						num=Integer.parseInt(str); 	
						break;
					}
					else break;
				}
				System.out.println(num);
				sss=null;
			}
			catch (Exception e)
			{
				jishu++;
				break;
			}
			 jishu++;
		}//while
		System.out.println("���ɨ������"+jishu+"�Σ�");
		try
		{
			Thread.currentThread().sleep(5000);
		}
		catch (Exception e)
		{
		}
		
		while(true){
			try
			{
				int a1=Integer.parseInt(reader.readLine());
				setIp(a1*1000);
				writer.println(mysqlip.size());
				writer.flush();
				CountDownLatch ch3=new CountDownLatch(1);
				Thread ms3=new Thread(new MomSpider(rest,close,ch3,1));//�ڱ��λλ1ʱ��ǰ������������Ҫ��
				ms3.start();
				ch3.await();

			}
			catch (Exception e)
			{
				break;
			}
		}
		System.out.println("һ��ץȡ��ҳ�������");
	}
	public synchronized void setIp(int a){
		String sql="";
		String x;
			db1 = new Mysql();//����DBHelper���� 
			db1.setURL("internetspider");
			sql="select * from ActiveIp limit"+"  "+a+","+(a+1000);
			db1.setSQL(sql);
			db1.MysqlRun();
			try{
			ret = db1.ps.executeQuery();
			if(ret != null){
                while(ret.next()){
                    x = ret.getString(2);
					mysqlip.add(x);
				 }
				}
			}
			catch(SQLException ex){
					ex.printStackTrace();
					}
			db1.close();//�ر����� 
				
	}
	public synchronized void insert(){
		String sql="";
		int x=0;
		String s;
		db1 = new Mysql();//����DBHelper���� 
		db1.setURL("internetspider");
		for(int a=0;a<listip.size();a++){
			s=listip.get(a);
			//sql="insert ActiveIp(ip) values('"+s+"')";
			count++;
			sql="insert ActiveIp(id,ip) values('"+String.valueOf(count)+"','"+s+"')";
			db1.setSQL(sql);
			db1.MysqlRun();
			try{
					x=db1.ps.executeUpdate();
			} 
			catch(SQLException ex){
					ex.printStackTrace();
					}
			 //�ر����� 
				
			}
		db1.close();
		if(x!=0)System.out.println("��ӳɹ�");
		//else System.out.println("���ʧ��");
		listip.clear();
	}
	public static void main(String[] args) 
	{
		try
		{
			Sc sc= new Sc();
			
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
	}
}
