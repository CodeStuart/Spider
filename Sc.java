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
	static BlockingQueue<String> queue=new  LinkedBlockingQueue<String>();//初始ip池
	static BlockingQueue<String> open=new LinkedBlockingQueue<String>();//活跃ip
	static BlockingQueue<String> rest=new LinkedBlockingQueue<String>();//扫描一次发现的不活跃ip，将在扫描一次
	static BlockingQueue<String> close=new LinkedBlockingQueue<String>();//两次都不活跃，视为死亡ip
	static BlockingQueue<String> mysqlip=new LinkedBlockingQueue<String>();//数据库中ip
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
			writer.println("母蜘蛛一：线程连接成功!");
			writer.flush();
			String str=reader.readLine();
			num=Integer.parseInt(str); 	
			System.out.println("连接服务器成功!");
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
			//抓取网页到本地
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
								System.out.println(ip+"开放80端口");
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
			System.out.println("活跃的ip:"+a+"个");
			System.out.println("休闲的ip:"+b+"个");
			System.out.println("休闲加活跃ip:"+(a+b)+"个");
			System.out.println("总ip"+(load*(jishu+1))+"个");
			System.out.println("开始第二次扫描...");
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
			System.out.println("第二次扫描结果");
			System.out.println("活跃ip共:"+open.size()+"个");
			System.out.println("新发现的活跃的ip:"+(open.size()-a)+"个");
			System.out.println("不活跃的有:"+b+"个");
			System.out.println((t2-t1)+"ms");
			str="";
			try
			{	
				String sss;
				sss="母蜘蛛一:扫描线程完毕!扫描了"+(jishu+1)*load+"个ip";
				if(open.size()>0){
					ms.stop();
					ms=null;
					ms2.stop();
					ms2=null;
					addQueue();
					if(a1>0)
					sss="母蜘蛛一:扫描线程完毕!\n母蜘蛛一:存储数据......存储了"+a1+"个ip";
					else 
					sss="母蜘蛛一:扫描线程完毕!";
					}
				if(close.size()>200000){
					deleteQueue(close);
					sss+="删除冗余数据......删除了"+(close.size())+"个ip";
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
		System.out.println("完成扫描任务"+jishu+"次！");
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
				Thread ms3=new Thread(new MomSpider(rest,close,ch3,1));//在标记位位1时，前两个参数不重要。
				ms3.start();
				ch3.await();

			}
			catch (Exception e)
			{
				break;
			}
		}
		System.out.println("一轮抓取网页任务完成");
	}
	public synchronized void setIp(int a){
		String sql="";
		String x;
			db1 = new Mysql();//创建DBHelper对象 
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
			db1.close();//关闭连接 
				
	}
	public synchronized void insert(){
		String sql="";
		int x=0;
		String s;
		db1 = new Mysql();//创建DBHelper对象 
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
			 //关闭连接 
				
			}
		db1.close();
		if(x!=0)System.out.println("添加成功");
		//else System.out.println("添加失败");
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
