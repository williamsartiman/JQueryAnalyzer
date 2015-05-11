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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import javolution.util.FastList;

import Components.MainWindow;
import Components.SQLConnectionManager;
import Components.MainWindowComponents.JQDialog;

public class MR_PrefixFinder {
	private JQDialog _FRAME;
	private Font _FONT = null;
	private JTextArea _LOG;
	private SQLConnectionManager _CONNECTION;
	private FastList<String> _TABLE_LIST = new FastList<String>();
	private Thread _ACTIVE_THREAD;
	
	/** -CONSTRUTOR- */
	public MR_PrefixFinder(SQLConnectionManager connection) {
		_CONNECTION = connection;
	}

	public void startPrograma() {
		_FONT = new Font("Verdana", Font.ROMAN_BASELINE, 10);

		_FRAME = new JQDialog(MainWindow.getMainFrame(), "JQueryAnalizer [MRDigital] - Localiza e lista de forma ordenada os Prefixos de PI utilizados no publi e os prefixos ainda dispon�veis.");
		_FRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		_FRAME.setPreferredSize(new Dimension(550, 400));
		_FRAME.setMaximumSize(new Dimension(550, 400));
		_FRAME.setMinimumSize(new Dimension(550, 400));
		_FRAME.setLocationRelativeTo(null);
		_FRAME.getGlassPane().setBackground(new Color(180, 191, 222));
		_FRAME.setResizable(false);
		_FRAME.getContentPane().setLayout(null);
		_FRAME.addWindowListener(new WindowListener(){

			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				System.out.println("Encerrando a normaliza��o por requisi��o do usu�rio");
				
				_FRAME.dispose();
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
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
				try {
					FastList<String> list = new FastList<String>();
					_LOG.append("� Localizando tabelas de CLIENTES e de PIT's, seja paciente!!!\n");
					for (String table : _CONNECTION.getTables()) {
						if (table != null && (table.toLowerCase().contains("pit") || table.toLowerCase().contains("cli")) && (table.endsWith("1") || table.endsWith("1") || table.endsWith("2") || table.endsWith("3") || table.endsWith("4") || table.endsWith("5") || table.endsWith("6") || table.endsWith("7") || table.endsWith("8") || table.endsWith("9") || table.endsWith("0"))) {
							_LOG.append("*** " + table + "\n");
							list.add(table);
						}
					}
					if (list.size() > 0) {
						_TABLE_LIST = list;
						Execute exec = new Execute();
						_ACTIVE_THREAD = new Thread(exec);
						_ACTIVE_THREAD.start();
					}
				}
				catch (Exception e) { e.printStackTrace(); }
			}
		});

		_FRAME.pack();
		_FRAME.setVisible(true);
	}
	
	private class Execute implements Runnable {
		@Override
		public void run() {
			String sql = "SELECT DISTINCT t.prefixo FROM (";
			int n = 0;
			for (String table : _TABLE_LIST.toArray(new String[_TABLE_LIST.size()])) {
				++n;
				if (table != null) {
					sql += (n > 1 ? " UNION ALL " : "") + (table.toLowerCase().contains("pit") ? "SELECT DISTINCT (CASE WHEN pit IS NOT NULL THEN LEFT(pit, 2) ELSE '' END) AS 'prefixo' FROM " + table + " WHERE pit IS NOT NULL AND pit<>''" : "SELECT DISTINCT prefixo_pi AS 'prefixo' FROM " + table + " WHERE prefixo_pi IS NOT NULL AND prefixo_pi<>''");
				}
			}
			sql += ") AS t";
			System.out.println(sql);
			try {
				ResultSet rs = _CONNECTION.executeQuery(sql);
				FastList<String> prefixos_usados = new FastList<String>();
				while (rs.next()) {
					prefixos_usados.add(rs.getString(1));
				}
				
				FastList<String> prefixos_possiveis = new FastList<String>();
				String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ_1234567890.-=+";
				for (int i = 0; charset.length() > i; i++) {
					for (int j = 0; charset.length() > j; j++) {
						prefixos_possiveis.add(charset.charAt(i) + "" + charset.charAt(j));
					}
				}
				_LOG.append("� O mapa de caracteres utilizado no processo foi: '" + charset + "'.\n");
				_LOG.append("� J� foram utilizados: " + prefixos_usados.size() + " c�digos de prefixos distintos de um total de: " + prefixos_possiveis.size() + " combina��es possiveis.\n");
				_LOG.append("� Segue abaixo a lista dos c�digos de prefixos J� UTILIZADOS:\n");
				for (int i = 0; prefixos_usados.size() > i; i++) {
					if (i > 0 && i % 20 == 0) {
						_LOG.append("\n");
					}
					_LOG.append("[" + prefixos_usados.get(i) + "]\t");
				}
				_LOG.append("\n");
				FastList<String> prefixos_disponiveis = new FastList<String>();
				boolean exists = false;
				for (int i = 0; prefixos_possiveis.size() > i; i++) {
					exists = false;
					for (int j = 0; prefixos_usados.size() > j; j++) {
						if (exists) { break; }
						else if (prefixos_usados.get(j).equalsIgnoreCase(prefixos_possiveis.get(i))) {
							exists = true;
						}
					}
					if (!exists) {
						prefixos_disponiveis.add(prefixos_possiveis.get(i));
					}
				}
				_LOG.append("� Ainda existem: " + prefixos_disponiveis.size() + " c�digos de prefixos disponiveis para utiliza��o.\n");
				_LOG.append("� Segue abaixo a lista dos c�digos de prefixos DISPON�VEIS:\n");
				for (int i = 0; prefixos_disponiveis.size() > i; i++) {
					if (i > 0 && i % 20 == 0) {
						_LOG.append("\n");
					}
					_LOG.append("[" + prefixos_disponiveis.get(i) + "]\t");
				}
				_LOG.append("\n");
				_LOG.append("� Conclu�do!");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(sql);
		}		
	}
	
	
}