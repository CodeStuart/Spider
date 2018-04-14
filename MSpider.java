import java.net.*;
import java.util.*;
import java.sql.ResultSet;    
import java.sql.SQLException;
import java.util.concurrent.*;
import java.io.*;
import util.Mysql;
class MSpider 
{
	Mysql db1=null;
	static int index=0;
	static ResultSet ret = null;  
	ServerSocket serversocket;
	ArrayList<ClientThread> list=new ArrayList<ClientThread>();
	BufferedReader br;
	PrintWriter pw;
	Queue<Integer> num=new LinkedList<Integer>(); 
	static BlockingQueue<Integer> mysqlip=new LinkedBlockingQueue<Integer>();//数据库中ip
	MSpider(){
		System.out.println("控制台程序开始运行:");
		for(int i=0;i<300;i++)num.add(i);
		for(int i=0;i<100000;i++)mysqlip.add(i);
		try
		{
			serversocket=new ServerSocket(9999);
			Thread t=new ServerThread(serversocket);	
			t.start();
		}
		catch (Exception e)
		{
		}
	}
	class ServerThread extends Thread{
		ServerSocket serversocket;
		ServerThread(ServerSocket s){
			this.serversocket=s;
		}
		public void run(){
					long t1=System.currentTimeMillis();
					int TIME=0;
					//一直处于堵塞状态，如果有客户端进程进来，则接受进入一个循环
					while(true){
						try{
							//if(list.size()==0)TIME++;
							//if(TIME==2)break;//第二次TIME=2说明第二次list.size=0,所开线程全都关闭了
							Socket socket=serversocket.accept();
							ClientThread ct=new ClientThread(socket);
							ct.start();
							list.add(ct);
							
						}
						catch(Exception e){
							e.printStackTrace();
						}
				}	
				//long t2=System.currentTimeMillis();
				//System.out.println("总时间为"+(t2-t1)+"ms");
		}
	}
	
	class ClientThread extends Thread{
		BufferedReader reader1;
		PrintWriter writer1;
		Socket socket;
		ClientThread(Socket socket){
			try{
				this.socket=socket;
				//接受用户端基本信息
				reader1=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer1=new PrintWriter(socket.getOutputStream());
				//父蜘蛛扫完一遍，向母蜘蛛发送信息，母蜘蛛发送一个节点，直到节点完毕。期间暂停
					//发送的一个数值，接受的是父蜘蛛信息
			}
				catch(Exception e){}
			}
		public void run(){
			try{
				while(true){
						String str=reader1.readLine();
						if(str!=""||str!=null){
							int count=getNum();
							if(count==-1)continue;
							writer1.println(count);
							System.out.println(this+str);
							writer1.flush();
						}
						if(num.size()<list.size())break;//
						}
					writer1.println("break");
					writer1.flush();
					int c=getIpNum();
					writer1.println(c);
					writer1.flush();
					int sz=0;
						while(true){//发送数据库数据
						String str=reader1.readLine();
						if(str!=""||str!=null){
							c=getIpNum();
							try
							{
								int cs=Integer.parseInt(str);
								System.out.println(this+"抓取网页中");
								sz=1;
								if(cs==0||c==0)break;
								System.out.println(str);
								writer1.println(c);
								writer1.flush();
							}
							catch (Exception t)
							{
								System.out.println(str);		 
							}
							 
						}
					}
					writer1.println("break");
					writer1.flush();
				}
			catch(IOException e){}
			finally{
				try
				{
					System.out.println(this+"关闭连接");
					socket.close();
					//list.remove(this);
				}
				catch (Exception qwe)
				{
					//list.remove(this);
				}
			}
		}
	}
	public synchronized int getNum(){
		if(num.size()>0)
			return num.poll();
		else return -1;
	}
	public synchronized int getIpNum(){
		if(mysqlip.size()>0)
			return mysqlip.poll();
		else return 0;
	}
	public int getSqlNum(){
		String sql="";
		int x=0;
		String s;
		db1 = new Mysql();//创建DBHelper对象 
		db1.setURL("internetspider");
		sql="select count(*) from ActiveIp";
		db1.setSQL(sql);
		db1.MysqlRun();
			try{
			ret = db1.ps.executeQuery();
			if(ret != null){
                while(ret.next()){
                    x = ret.getInt(1);
                }
			}
		}
			catch(SQLException ex){
					ex.printStackTrace();
					}
			db1.close();//关闭连接 
			return x;		
		}
	public static void main(String[] args) 
	{
		new MSpider();
	}
}
