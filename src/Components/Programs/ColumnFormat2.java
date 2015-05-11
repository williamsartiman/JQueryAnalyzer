package Components.Programs;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import Components.MainWindow;
import Components.SQLConnectionManager;
import Components.MainWindowComponents.JQDialog;

public class ColumnFormat2 {
	private JQDialog _FRAME;
	private Font _FONT = null;
	private JTextArea _LOG;
	private SQLConnectionManager _CONNECTION;
	private int _TABLE_LIST_CHECKED;
	private int _TABLE_LIST_SIZE;
	private Thread[] _THREAD_LIST = null;
	private int _THREAD_PROCESS;
	
	/** -CONSTRUTOR- */
	public ColumnFormat2(SQLConnectionManager connection) {
		_CONNECTION = connection;
	}

	public void startPrograma() {
		_FONT = new Font("Verdana", Font.ROMAN_BASELINE, 10);

		_FRAME = new JQDialog(MainWindow.getMainFrame(), "JQueryAnalizer [MRDigital] - Normaliza banco de dados para instala��o do PubliNET");
		_FRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		_FRAME.setPreferredSize(new Dimension(550, 400));
		_FRAME.setMaximumSize(new Dimension(550, 400));
		_FRAME.setMinimumSize(new Dimension(550, 400));
		_FRAME.setLocationRelativeTo(null);
		_FRAME.getGlassPane().setBackground(new Color(180, 191, 222));
		_FRAME.setResizable(false);
		_FRAME.getContentPane().setLayout(null);
		_FRAME.addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent arg0) { }
			public void windowClosed(WindowEvent arg0) { }
			@SuppressWarnings("deprecation")
			public void windowClosing(WindowEvent arg0) {
				System.out.println("Encerrando a normaliza��o por requisi��o do usu�rio");
				if (_THREAD_PROCESS > 0) {
					int option = JOptionPane.showConfirmDialog(null, "Existe um processo de normaliza��o ativo! Tem certeza que deseja interromper o processo?", "JQueryAnalizer - Confirma��o", JOptionPane.YES_NO_OPTION);
					if (option == JOptionPane.YES_OPTION) {
						for (Thread t : _THREAD_LIST) {
							if (t.isAlive()) {
								t.stop();
							}
								
						}
						_THREAD_LIST = null;
						_THREAD_PROCESS = 0;
						_FRAME.dispose();
					}
					return;
				}
				_THREAD_LIST = null;
				_THREAD_PROCESS = 0;
				_FRAME.dispose();
			}
			public void windowDeactivated(WindowEvent arg0) { }
			public void windowDeiconified(WindowEvent arg0) { }
			public void windowIconified(WindowEvent arg0) { }
			public void windowOpened(WindowEvent arg0) { }
		});
		
		_LOG = new JTextArea();
		_LOG.setFont(new Font("Courier New", Font.ROMAN_BASELINE, 14));
		JScrollPane b1 = new JScrollPane(_LOG);
		b1.setBounds(10, 10, 525, 310);
		b1.setBorder(new LineBorder(Color.GRAY, 1, true));
		b1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		_FRAME.getContentPane().add(b1);

		
		JButton copiar = new JButton("Copiar");
		copiar.setBounds(165, 325, 150, 35);
		copiar.setFont(_FONT);
		_FRAME.add(copiar);
		
		copiar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Clipboard teclado = Toolkit.getDefaultToolkit().getSystemClipboard();  
				StringSelection selecao = new StringSelection(_LOG.getText()); 
				teclado.setContents(selecao, null);  
			}
			
		});

		
		JButton executar = new JButton("Iniciar");
		executar.setBounds(10, 325, 150, 35);
		executar.setFont(_FONT);
		_FRAME.add(executar);
		
		executar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent b) {
				
				_TABLE_LIST_CHECKED = 0;
				_THREAD_PROCESS = 0;
				
				try {
					List<String> list = _CONNECTION.getTables();
					Iterator<String> iterator = list.iterator();
					_THREAD_LIST = new Thread[list.size()];
					_TABLE_LIST_SIZE = list.size();
					_LOG.append("[#] Iniciando verifica��o das tabelas, buscando campos: DTINCLUSAO, USINCLUSAO, USMANU, DTMANU, HRMANU, OPMANU e ajustando estes campos conforme necess�rio.\n *** Padr�es adotados:\n      USMANU => VarChar(12)\n      DTMANU => VarChar(6)\n       OPMANU => VarChar(1)\n      USINCLUSAO => VarChar(12) \n      DTINCLUSAO => DATETIME\n[#] "+list.size()+" tabelas encontradas.\n");
					while (iterator.hasNext()) {
						Execute e = new Execute(iterator.next());
						Thread t = new Thread(e);
						_THREAD_LIST[_THREAD_PROCESS++] = t;
					}
					if (_THREAD_LIST[0] != null) {
						_THREAD_PROCESS = 0;
						_THREAD_LIST[_THREAD_PROCESS].start();
					}
				}
				catch (Exception e) { e.printStackTrace(); }
			}
		});

		_FRAME.pack();
		_FRAME.setVisible(true);
	}
	
	private class ColumnData {
		@SuppressWarnings("unused")
		private String column_name;
		public int column_type;
		public int column_size;
		public int status; // 0 = ok | 1 = add | 2 = modify
		
		public ColumnData(String name) {
			column_name = name;
			status = 1;
		}
		
		
	}
	
	private class Execute implements Runnable {
		private String table;
		public Execute(String t) {
			table = t;
		}
		@Override
		public void run() {
			_LOG.append("� "+((100.f * _TABLE_LIST_CHECKED / _TABLE_LIST_SIZE)+"00000").substring(0, 5)+"% concluido - processando a tabela `"+table.toUpperCase()+"`\n");
			Exception pass = null;
			try {
				ResultSet rs = _CONNECTION.executeQuery("SELECT * FROM " + table + " WHERE 1=0");
				ResultSetMetaData md = rs.getMetaData();
				ColumnData dtinclusao = new ColumnData("DTINCLUSAO");
				ColumnData usinclusao = new ColumnData("USINCLUSAO");
				ColumnData usmanu = new ColumnData("USMANU");
				ColumnData dtmanu = new ColumnData("DTMANU");
				ColumnData hrmanu = new ColumnData("HRMANU");
				ColumnData opmanu = new ColumnData("OPMANU");
				for (int i = 1; i <= md.getColumnCount(); i++) {
					if (md.getColumnName(i) == null || md.getColumnName(i).isEmpty()) {
						continue;
					}
					else if (md.getColumnName(i).equalsIgnoreCase("dtinclusao")) {
						switch (md.getColumnType(i)) {
							case java.sql.Types.DATE:
							case java.sql.Types.TIME:
							case java.sql.Types.TIMESTAMP:
								dtinclusao.column_type = md.getColumnType(i);
								dtinclusao.status = 0;
								break;
							default:
								dtinclusao.column_type = md.getColumnType(i);
								dtinclusao.status = 2;
						}
					}
					else if (md.getColumnName(i).equalsIgnoreCase("usinclusao")) {
						switch (md.getColumnType(i)) {
							case java.sql.Types.VARCHAR:
							case java.sql.Types.CHAR:
								usinclusao.column_type = md.getColumnType(i);
								usinclusao.column_size = md.getPrecision(i);
								usinclusao.status = (md.getPrecision(i) == 12 ? 0 : 2);
								break;
							default:
								usinclusao.column_type = md.getColumnType(i);
								usinclusao.status = 2;
						}
					}
					else if (md.getColumnName(i).equalsIgnoreCase("usmanu")) {
						switch (md.getColumnType(i)) {
							case java.sql.Types.VARCHAR:
							case java.sql.Types.CHAR:
								usmanu.column_type = md.getColumnType(i);
								usmanu.status = (md.getPrecision(i) == 12 ? 0 : 2);
								break;
							default:
								usmanu.column_type = md.getColumnType(i);
								usmanu.status = 2;
						}
					}
					else if (md.getColumnName(i).equalsIgnoreCase("dtmanu")) {
						switch (md.getColumnType(i)) {
							case java.sql.Types.VARCHAR:
							case java.sql.Types.CHAR:
								dtmanu.column_type = md.getColumnType(i);
								dtmanu.status = (md.getPrecision(i) == 6 ? 0 : 2);
								break;
							default:
								dtmanu.column_type = md.getColumnType(i);
								dtmanu.status = 2;
						}
					}
					else if (md.getColumnName(i).equalsIgnoreCase("hrmanu")) {
						switch (md.getColumnType(i)) {
							case java.sql.Types.VARCHAR:
							case java.sql.Types.CHAR:
								hrmanu.column_type = md.getColumnType(i);
								hrmanu.status = (md.getPrecision(i) == 6 ? 0 : 2);
								break;
							default:
								hrmanu.column_type = md.getColumnType(i);
								hrmanu.status = 2;
						}
					}					
					else if (md.getColumnName(i).equalsIgnoreCase("opmanu")) {
						switch (md.getColumnType(i)) {
							case java.sql.Types.VARCHAR:
							case java.sql.Types.NCHAR:
							case java.sql.Types.NVARCHAR:
							case java.sql.Types.CHAR:
								opmanu.column_type = md.getColumnType(i);
								opmanu.status = (md.getPrecision(i) == 1 ? 0 : 2);
								break;
							default:
								opmanu.column_type = md.getColumnType(i);
								opmanu.status = 2;
						}
					}
				}
				String sql = null;
				sql = "ALTER TABLE " + table + " " + (dtinclusao.status == 2 ? "MODIFY" : "ADD") + " COLUMN DTINCLUSAO DATETIME NULL";
				if (dtinclusao.status != 0) {
					pass = _CONNECTION.executeUpdate(sql);
				}
				if (pass == null) {
					_LOG.append("     DTINCLUSAO - " + (dtinclusao.status == 0 ? "V�LIDO" : (sql.contains("MODIFY") ? "ALTERADO" : "ADICIONADO")) + " *** OK.\n");				
				}
				else {
					_LOG.append("     DTINCLUSAO -> " + sql + " *** [" + pass.getMessage() + "] \n");	
				}
				// --
				sql = "ALTER TABLE " + table + " " + (usinclusao.status == 2 ? "MODIFY" : "ADD") + " COLUMN USINCLUSAO VARCHAR(12)";
				if (usinclusao.status != 0) {
					pass = _CONNECTION.executeUpdate(sql);
				}
				if (pass == null) {
					_LOG.append("     USINCLUSAO - " + (usinclusao.status == 0 ? "V�LIDO" : (sql.contains("MODIFY") ? "ALTERADO" : "ADICIONADO")) + " *** OK.\n");				
				}
				else {
					_LOG.append("     USINCLUSAO -> " + sql + " *** [" + pass.getMessage() + "] -> " + usinclusao.status + " " + usinclusao.column_type + " " + usinclusao.column_size + " \n");
					System.out.println("STATUS:");
					System.out.println(usinclusao.status);
					System.out.println("COLUMN_TYPE:");
					System.out.println(usinclusao.column_type);
					System.out.println("COLUMN_SIZE:");
					System.out.println(usinclusao.column_size);
				}
				// --
				sql = "ALTER TABLE " + table + " " + (usmanu.status == 2 ? "MODIFY" : "ADD") + " COLUMN USMANU VARCHAR(12)";
				if (usmanu.status != 0) {
					pass = _CONNECTION.executeUpdate(sql);
				}
				if (pass == null) {
					_LOG.append("     USMANU - " + (usmanu.status == 0 ? "V�LIDO" : (sql.contains("MODIFY") ? "ALTERADO" : "ADICIONADO")) + " *** OK.\n");				
				}
				else {
					_LOG.append("     USMANU -> " + sql + " *** [" + pass.getMessage() + "] \n");	
				}
				// --
				sql = "ALTER TABLE " + table + " " + (dtmanu.status == 2 ? "MODIFY" : "ADD") + " COLUMN DTMANU VARCHAR(6)";
				if (dtmanu.status != 0) {
					pass = _CONNECTION.executeUpdate(sql);
				}
				if (pass == null) {
					_LOG.append("     DTMANU - " + (dtmanu.status == 0 ? "V�LIDO" : (sql.contains("MODIFY") ? "ALTERADO" : "ADICIONADO")) + " *** OK.\n");
				}
				else {
					_LOG.append("     DTMANU -> " + sql + " *** [" + pass.getMessage() + "] \n");	
				}
				// --
				sql = "ALTER TABLE " + table + " " + (hrmanu.status == 2 ? "MODIFY" : "ADD") + " COLUMN HRMANU VARCHAR(12)";
				if (hrmanu.status != 0) {
					pass = _CONNECTION.executeUpdate(sql);
				}
				if (pass == null) {
					_LOG.append("     HRMANU - " + (hrmanu.status == 0 ? "V�LIDO" : (sql.contains("MODIFY") ? "ALTERADO" : "ADICIONADO")) + " *** OK.\n");				
				}
				else {
					_LOG.append("     HRMANU -> " + sql + " *** [" + pass.getMessage() + "] \n");	
				}
				// --
				sql = "ALTER TABLE " + table + " " + (opmanu.status == 2 ? "MODIFY" : "ADD") + " COLUMN OPMANU VARCHAR(1)";
				if (opmanu.status != 0) {
					pass = _CONNECTION.executeUpdate(sql);
				}
				if (pass == null) {
					_LOG.append("     OPMANU - " + (opmanu.status == 0 ? "V�LIDO" : (sql.contains("MODIFY") ? "ALTERADO" : "ADICIONADO")) + " *** OK.\n");				
				}
				else {
					_LOG.append("     OPMANU -> " + sql + " *** [" + pass.getMessage() + "] \n");	
				}
			}
			catch (Exception e) {
				_LOG.append(e.getMessage() + "\n");
				e.printStackTrace();
			}			
			_LOG.setCaretPosition(_LOG.getText().length());
			if (_THREAD_PROCESS + 1 >= _TABLE_LIST_SIZE) {
			//if (_TABLE_LIST_CHECKED >= _TABLE_LIST_SIZE) {
				_LOG.append("[#] Verifica��o concluida!");
				_THREAD_PROCESS = 0;
			}
			else {
				_THREAD_LIST[++_THREAD_PROCESS].start();
			}
			_TABLE_LIST_CHECKED += 1;
		}		
	}
	
	
}