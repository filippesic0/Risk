package net.yura.domination.lobby.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.yura.domination.engine.OnlineUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.swing.GraphicsUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mapstore.GetMap;
import net.yura.domination.mapstore.Map;
import net.yura.domination.ui.flashgui.NewGameFrame;
import net.yura.lobby.model.Game;
import net.yura.swing.HintTextField;
import net.yura.swing.ImageIcon;
import net.yura.swing.SpriteIcon;

/**
 * @author Yura Mamyrin
 * @author Michiel Pater
 */
public class GameSetupPanel extends JPanel implements ActionListener {

	private BufferedImage newgame;
	private BufferedImage game2;
	private BufferedImage card;

	private JLabel mapPic;
	private JPanel mapsMissions;

	private JButton start;
	//private JButton help;
	private JButton cancel;

	private JRadioButton domination;
	private JRadioButton capital;
	private JRadioButton mission;

	private JRadioButton fixed;
	private JRadioButton increasing;
        private JRadioButton italian;

	private JCheckBox AutoPlaceAll;
	private JCheckBox recycle;

	private ResourceBundle resb;

	private String options;
	private HintTextField search;
	private JList mapList; // not using generics for java 1.6 support

	private JDialog dialog;
	private RiskMap riskmap;

	private JSpinner human;
	private JSpinner aieasy;
        private JSpinner aiaverage;
	private JSpinner aihard;

	private JTextField gamename;
        private JCheckBox passwordCheckbox;
        private JTextField passwordField;
        private JComboBox timeout;

        private Set<RiskMap> downloading = Collections.synchronizedSet(new HashSet());

	public GameSetupPanel() {

                // ALL PROBLEMS COME FROM THIS!!!! (in applet mode)
                // when this was at the start of the init loads of class problems started
                resb = TranslationBundle.getBundle();

		setLayout(null);

		Dimension d = GraphicsUtil.newDimension(700, 600);

		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);


		try {
			newgame = ImageIO.read( NewGameFrame.class.getResource("newgame.jpg") );
			game2 = ImageIO.read( getClass().getResource("game2.jpg") );

			card = game2.getSubimage(0,247,23,35);

		}
		catch (IOException ex) { throw new RuntimeException(ex); }










		mapPic = new JLabel();
		GraphicsUtil.setBounds(mapPic, 51, 51, 203, 127);
		add(mapPic);


		mapsMissions = new JPanel();
		mapsMissions.setOpaque(false);
		mapsMissions.setLayout(new javax.swing.BoxLayout(mapsMissions, javax.swing.BoxLayout.Y_AXIS));

		final JScrollPane sp2 = new JScrollPane(mapsMissions);
		GraphicsUtil.setBounds(sp2, 340, 51, 309, 200); // this will allow 6 players, 30 pixels per player
		sp2.setBorder(null);

		sp2.setOpaque(false);
                sp2.getViewport().setBackground(new Color(0x00000000, true)); // needed for java1.5 Linux GTKTheme
		sp2.getViewport().setOpaque(false);
                sp2.setViewportBorder(null); // for nimbus

		add( sp2 );










		mapList = new JList();
		mapList.setCellRenderer(new RiskMapListCellRenderer());
                mapList.setFixedCellWidth(10); // will stretch
		//mapList.setVisibleRowCount(10);
		mapList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
                mapList.setBackground(new Color(0x00000000, true)); // needed for java1.5 Linux GTKTheme
		mapList.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(mapList);
		scrollPane.setBorder(null);
                scrollPane.setOpaque(false);
                scrollPane.getViewport().setBackground(new Color(0x00000000, true)); // needed for java1.5 Linux GTKTheme
		scrollPane.getViewport().setOpaque(false);
                scrollPane.setViewportBorder(null); // for nimbus

		search = new HintTextField(resb.getString("newgame.map.filter"));
		
		// Listen for changes in the text
		search.getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        search();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        search();
                    }

                    public void insertUpdate(DocumentEvent e) {
                        search();
                    }

                    public void search() {
                        final RiskMap[] availableMaps = getAllAvailableMaps();
                        RiskMap[] mapsToDisplay;

                        String searchText = search.getText().toLowerCase();
                        int indexToSelect = -1;
                        if ("".equals(searchText)) {
                            mapsToDisplay = availableMaps;
                            indexToSelect = Arrays.asList(mapsToDisplay).indexOf(riskmap);
                        }
                        else {
                            ArrayList<RiskMap> filteredMaps = new ArrayList<RiskMap>();
                            for (int c = 0; c < availableMaps.length; c++) {
                                RiskMap r = availableMaps[c];

                                if (r.getID().toLowerCase().contains(searchText)
                                        // TODO getMap is null for any map whos icon has not be requested
                                        //|| (r.getMap() != null  &&  r.getMap().getName().toLowerCase().contains( search.getText().toLowerCase() ) )
                                        ) {
                                    filteredMaps.add(r);
                                    if (r == riskmap) {
                                        indexToSelect = filteredMaps.size() - 1;
                                    }
                                }
                            }
                            mapsToDisplay = filteredMaps.toArray(new RiskMap[filteredMaps.size()]);
                        }
                        mapList.setListData(mapsToDisplay);
                        if (indexToSelect >= 0) {
                            mapList.setSelectedIndex(indexToSelect);
                        }
                    }
		});

		JPanel mapsPanel = new JPanel(new BorderLayout());
		mapsPanel.setOpaque(false);
		GraphicsUtil.setBounds(mapsPanel, 54, 192, 200, 260);
		mapsPanel.add(search, BorderLayout.NORTH);
		mapsPanel.add(scrollPane, BorderLayout.CENTER);
		add(mapsPanel);



		mapList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if (e.getValueIsAdjusting()) {return;}

				final RiskMap it = (RiskMap) mapList.getSelectedValue();

				if (it!=null) {

                                    if (!it.isLocalMap()) {
                                        // no easy way to make modal JInternalFrame, as JOptionPane uses Container.startLWModal voodoo

                                        // reset the list
                                        int oldItemIndex = getIndexOfItem(mapList, riskmap);
                                        if (oldItemIndex >= 0) {
                                            // setSelectedValue wont cut it as it does not clear selection if item is not found
                                            mapList.setSelectedIndex(oldItemIndex);
                                        }
                                        else {
                                            mapList.clearSelection();
                                        }
                                        
                                        if(downloading.contains(it)) {
                                            return;
                                        }
                                        
                                        String[] message = { "Do you want to Download", "map: " + it.toString() };

                                        // if we have more info about the map then show it.
                                        Map map = it.getMap();
                                        if (map != null) {
                                            message = new String[] { message[0], message[1],
                                            "By: " + map.getAuthorName(),
                                            "Downloads: " + map.getNumberOfDownloads(),
                                            "Version: " + map.getVersion(),
                                            map.getDescription()};
                                        }

                                        int result = showConfirmDialog(GameSetupPanel.this, message, "Download?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, it.getIcon(203, 127, GameSetupPanel.this));

                                        if (result == JOptionPane.OK_OPTION) {
                                            downloading.add(it);

                                            GetMap.getMap(it.getID(), new Observer() {
                                                public void update(Observable o, Object result) {
                                                    downloading.remove(it);

                                                    if (result == RiskUtil.SUCCESS) {
                                                        mapList.setSelectedValue(it, true);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                    else {
					riskmap = it;
					mapPic.setIcon(riskmap.getIcon(203, 127, mapPic));

                                        mapsMissions.removeAll();

                                        String[] missions = riskmap.getMissions();

                                        for (int c=0;c<missions.length;c++) {
                                                mapsMissions.add( makeNewMission(missions[c]) );
                                                mapsMissions.add( Box.createVerticalStrut(3) );
                                        }
                                        if (missions.length == 0) {
                                                JLabel noMissionsLabel = new JLabel(" " + resb.getString("newgame.missions.none"));
                                                noMissionsLabel.setForeground(Color.BLACK); // for gtk dark theme
                                                mapsMissions.add(noMissionsLabel);
                                                if (mission.isSelected()) {
                                                    domination.setSelected(true);
                                                    AutoPlaceAll.setEnabled(true);
                                                }
                                        }
                                        mission.setEnabled( missions.length != 0 );

                                        mapsMissions.revalidate();
                                        // @TODO: scroll to the top
                                    }
                                }
			}
		});





		human = new JSpinner( new SpinnerNumberModel(2,1,6,1) );
		aiaverage = new JSpinner( new SpinnerNumberModel(0,0,6,1) );
		aieasy = new JSpinner( new SpinnerNumberModel(2,0,6,1) );
		aihard = new JSpinner( new SpinnerNumberModel(2,0,6,1) );


		JComponent playernum = new JPanel(new GridBagLayout());
		GraphicsUtil.setBounds(playernum, 320, 280, 350, 60);
		playernum.setOpaque(false);
		add(playernum);
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.BOTH;
                
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.insets = new Insets(0,0,0,5);
		playernum.add(newShrinkJLabel( resb.getString("newgame.player.type.human") ), gbc);
                gbc.gridx = 1;
                gbc.gridy = 0;
                gbc.insets = new Insets(0,0,0,0);
		playernum.add(human, gbc);
                gbc.gridx = 2;
                gbc.gridy = 0;
                gbc.insets = new Insets(0, 15, 0, 5);
		playernum.add(newShrinkJLabel( resb.getString("newgame.player.type.easyai") ), gbc);
                gbc.gridx = 3;
                gbc.gridy = 0;
                gbc.insets = new Insets(0,0,0,0);
		playernum.add(aieasy, gbc);
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.insets = new Insets(5,0,0,5);
		playernum.add(newShrinkJLabel( resb.getString("newgame.player.type.averageai") ), gbc);
                gbc.gridx = 1;
                gbc.gridy = 1;
                gbc.insets = new Insets(5,0,0,0);
		playernum.add(aiaverage,gbc);
                gbc.gridx = 2;
                gbc.gridy = 1;
                gbc.insets = new Insets(5, 15, 0, 5);
                playernum.add(newShrinkJLabel( resb.getString("newgame.player.type.hardai") ), gbc);
                gbc.gridx = 3;
                gbc.gridy = 1;
                gbc.insets = new Insets(5,0,0,0);
		playernum.add(aihard, gbc);
                
		ButtonGroup GameTypeButtonGroup = new ButtonGroup();
		ButtonGroup CardTypeButtonGroup = new ButtonGroup();



                int bw1 = 119;
                int bw2 = 180;
                int bh = 25;

		domination = new JRadioButton(resb.getString("newgame.mode.domination"), true);
		NewGameFrame.sortOutButton( domination );
		GraphicsUtil.setBounds(domination, 380, 370, bw1, bh);
		domination.addActionListener(this);

		capital = new JRadioButton(resb.getString("newgame.mode.capital"));
		NewGameFrame.sortOutButton( capital );
		GraphicsUtil.setBounds(capital, 380, 390, bw1, bh);
		capital.addActionListener(this);

		mission = new JRadioButton(resb.getString("newgame.mode.mission"));
		NewGameFrame.sortOutButton( mission );
		GraphicsUtil.setBounds(mission, 380, 410, bw1, bh);
		mission.addActionListener(this);

                

		increasing = new JRadioButton(resb.getString("newgame.cardmode.increasing"),true);
		NewGameFrame.sortOutButton( increasing );
		GraphicsUtil.setBounds(increasing, 500, 370, bw2, bh);

		fixed = new JRadioButton(resb.getString("newgame.cardmode.fixed"));
		NewGameFrame.sortOutButton( fixed );
		GraphicsUtil.setBounds(fixed, 500, 390, bw2, bh);

                italian = new JRadioButton(resb.getString("newgame.cardmode.italianlike"));
		NewGameFrame.sortOutButton( italian );
		GraphicsUtil.setBounds(italian, 500, 410, bw2, bh);


		GameTypeButtonGroup.add ( domination );
		GameTypeButtonGroup.add ( capital );
		GameTypeButtonGroup.add ( mission );

		CardTypeButtonGroup.add ( fixed );
		CardTypeButtonGroup.add ( increasing );
                CardTypeButtonGroup.add ( italian );


		add(domination);
		add(capital);
		add(mission);

		add(fixed);
		add(increasing);
                add(italian);

		AutoPlaceAll = new JCheckBox(resb.getString("newgame.autoplace"));
		NewGameFrame.sortOutButton( AutoPlaceAll );
		GraphicsUtil.setBounds(AutoPlaceAll, 380, 440, bw1, bh);

		recycle = new JCheckBox(resb.getString("newgame.recycle"));
		NewGameFrame.sortOutButton( recycle );
		GraphicsUtil.setBounds(recycle, 500, 440, bw2, bh);

		add(AutoPlaceAll);
		add(recycle);

		int w=115;
		int h=31;

		cancel = new JButton(resb.getString("newgame.cancel"));
		NewGameFrame.sortOutButton( cancel , newgame.getSubimage(41, 528, w, h) , newgame.getSubimage(700, 233, w, h) , newgame.getSubimage(700, 264, w, h) );
		cancel.addActionListener( this );
		GraphicsUtil.setBounds(cancel, 41, 528, 115, 31);

		//help = new JButton();
		//NewGameFrame.sortOutButton( help , newgame.getSubimage(335, 528, 30 , 30) , newgame.getSubimage(794, 171, 30 , 30) , newgame.getSubimage(794, 202, 30 , 30) );
		//help.addActionListener( this );
		//help.setBounds(335, 529, 30 , 30 ); // should be 528
                
                
                GridBagConstraints c = new GridBagConstraints();
                c.insets = GraphicsUtil.newInsets(2, 2, 2, 2);
                c.fill = GridBagConstraints.HORIZONTAL;
                final JComponent bottompanel = new JPanel(new java.awt.GridBagLayout());
                bottompanel.setOpaque(false);
                

                passwordCheckbox = new JCheckBox(resb.getString("newgame.private"));
                passwordCheckbox.setHorizontalAlignment(SwingConstants.RIGHT); // just in case name label is longer then password
                NewGameFrame.sortOutButton(passwordCheckbox); // needed for windows
                passwordCheckbox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        boolean privateGame = passwordCheckbox.isSelected();
                        passwordField.setVisible(privateGame);
                        bottompanel.revalidate();
                        bottompanel.repaint();
                        if (privateGame) {
                            passwordField.requestFocusInWindow();
                        }
                    }
                });
                c.gridx = 0; // col
                c.gridy = 1; // row
                c.gridwidth = 1; // width
                c.gridheight = 1; // height
                bottompanel.add(passwordCheckbox, c); // "Password"

                c.gridx = 0; // col
                c.gridy = 0; // row
                c.gridwidth = 1; // width
                c.gridheight = 1; // height
                JLabel nameLabel = new JLabel(resb.getString("newgame.label.name"), SwingConstants.RIGHT); // "Game Name:"
                nameLabel.setForeground(Color.BLACK); // os default may be white in dark gtk theme
                bottompanel.add(nameLabel, c);

                gamename = new JTextField();
                c.gridx = 1; // col
                c.gridy = 0; // row
                c.gridwidth = 1; // width
                c.gridheight = 1; // height
                c.weightx = 1;
		bottompanel.add(gamename, c);

                
                passwordField = new HintTextField(resb.getString("lobby.password"));
                passwordField.setVisible(false);
                c.gridx = 1; // col
                c.gridy = 1; // row
                c.gridwidth = 1; // width
                c.gridheight = 1; // height
		bottompanel.add(passwordField, c);
                
                c.gridx = 2; // col
                c.gridy = 0; // row
                c.gridwidth = 1; // width
                c.gridheight = 2; // height
                c.weightx = 0;
                bottompanel.add(Box.createHorizontalStrut(GraphicsUtil.scale(10)), c);
                
                int hour = 60*60;
                Timeout[] timeouts = new Timeout[] {
                    new Timeout("10sec",10),
                    new Timeout("20sec",20),
                    new Timeout("30sec",30),
                    new Timeout("1min",60),
                    new Timeout("5min",60*5),
                    new Timeout("10min",60*10),
                    new Timeout("20min",60*20),
                    new Timeout("30min",60*30),
                    new Timeout("1hour",hour),
                    new Timeout("2hours",hour*2),
                    new Timeout("6hours",hour*6),
                    new Timeout("12hours",hour*12),
                    new Timeout("24hours",hour*24)
                };
		timeout = new JComboBox(timeouts);
                timeout.setSelectedIndex(3 /* 1 minute */);
                
                c.gridx = 3; // col
                c.gridy = 1; // row
                c.gridwidth = 1; // width
                c.gridheight = 1; // height
                c.anchor = GridBagConstraints.NORTH;
		bottompanel.add(timeout, c);
                
                JLabel timeoutLabel = new JLabel(resb.getString("newgame.label.timeout"));
                timeoutLabel.setVerticalAlignment(SwingConstants.BOTTOM);
                timeoutLabel.setLabelFor(timeout);
                timeoutLabel.setForeground(Color.BLACK); // os default may be white in dark gtk theme
                c.gridx = 3; // col
                c.gridy = 0; // row
                c.gridwidth = 1; // width
                c.gridheight = 1; // height
                c.anchor = GridBagConstraints.SOUTH;
                bottompanel.add(timeoutLabel, c); // "Turn Timeout:"
                
                GraphicsUtil.setBounds(bottompanel, 170, 504, 360, 80); // should be 528
                add(bottompanel);

		start = new JButton(resb.getString("newgame.startgame"));
		NewGameFrame.sortOutButton( start , newgame.getSubimage(544, 528, w, h) , newgame.getSubimage(700, 295, w, h) , newgame.getSubimage(700, 326, w, h) );
		start.addActionListener( this );
		GraphicsUtil.setBounds(start, 544, 528, 115, 31);

		add(cancel);
		//add(help);
		add(start);

		mapList.setFixedCellHeight(GraphicsUtil.scale(33));
	}

        public static int showConfirmDialog(Component parentComponent,
                                        String[] message,
                                        String title, int optionType,
                                        int messageType, Icon icon) {

            // if lines are too long they push the buttons off to the right and make them impossible to click
            // using JTextArea to wrap the text pushes the button off the bottom and cut off.
            for (int c = 0; c < message.length; c++) {
                if (message[c].length() > 50) {
                    String[] split = message[c].split(RiskUtil.quote("\n"));
                    for (int i = 0; i < split.length; i++) {
                        if (split[i].length() > 50) {
                            message[c] = "<html><p style='width: " + GraphicsUtil.scale(200) + "px;'>" + message[c].replace("\n", "<br>");
                            break;
                        }
                    }
                }
            }

            // TODO document why we had to use Internal here
            return JOptionPane.showInternalConfirmDialog(parentComponent, message, title, optionType, messageType, icon);
        }

        private static int getIndexOfItem(JList list, Object obj) {
            ListModel model = list.getModel();
            for (int c = 0; c < model.getSize(); c++) {
                if (model.getElementAt(c) == obj) {
                    return c;
                }
            }
            return -1;
        }
        
        private JLabel newShrinkJLabel(String text) {
            JLabel label = new JLabel(text, SwingConstants.TRAILING);
            label.setForeground(Color.BLACK); // in case OS default is dark theme with white text
            return label;
        }

        /**
         * useful only really when using box layout
         */
        private void dontStretch(JComponent comp) {
            comp.setMaximumSize(new Dimension(comp.getMaximumSize().width, comp.getPreferredSize().height));
        }

        class Timeout {
            String name;
            int time;
            Timeout(String name,int time) {
                this.name = resb.getString("newgame.timeout."+name);
                this.time = time;
            }
            int getTime() {
                return time;
            }
            public String toString() {
                return name;
            }
        }

        private String newGameOptions;
        
        public Game showDialog(Window parent,String serveroptions, String defaultGameName) {
            
                gamename.setText(defaultGameName);
            
                if (dialog == null) {

                        // TODO parent is passed in each time but is only used the first time
                        dialog = newJDialog(parent, resb.getString("newgame.title.network"), true);

			// @todo:
			// do noting on close
			// when close then send event to gsp

			dialog.setContentPane(this);
			dialog.setResizable(false);
			dialog.pack();
		}


		if (serveroptions!=null && !serveroptions.equals(newGameOptions) ) {

			newGameOptions = serveroptions;
                        mapList.setListData(getAllAvailableMaps());
                        mapList.setSelectedIndex(0);
		}

		reset();

		dialog.setVisible(true);

		String op = getOptions();

		if (op != null) {
                    Game newGame = new Game( getGameName(), op, getNumberOfHumanPlayers(), ((Timeout)timeout.getSelectedItem()).getTime() );
                    if (passwordCheckbox.isSelected()) {
                        newGame.setMagicWord(passwordField.getText());
                    }
                    return newGame;
		}
		return null;
        }
        
        private RiskMap[] getAllAvailableMaps() {
            String[] split = newGameOptions.split(",");
            RiskMap[] maps = new RiskMap[split.length];
            for (int c = 0; c < maps.length; c++) {
                maps[c] = RiskMap.getMapIcon(decode(split[c]));
            }
            return maps;
        }

        static JDialog newJDialog(Window parent, String title, boolean modal) {
            //new JDialog(window, JDialog.DEFAULT_MODALITY_TYPE); java 1.6
            if (parent instanceof Frame) {
                return new JDialog((Frame) parent, title, modal);
            }
            return new JDialog((Dialog) parent, title, modal);
        }

        private static String decode(String text) {
            try {
                return URLDecoder.decode(text, "UTF-8");
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

	public JList getList() {
		return mapList;
	}

	public JPanel makeNewMission(String a) {

			JPanel mission = new JPanel() {
				public void paintComponent(Graphics g) {
					g.setColor( new Color(255,255,255,100) );
					g.fillRect(0,0,getWidth(),getHeight());
				}
			};

			//Dimension size = new Dimension(290, 35);

			//mission.setPreferredSize(size);
			//mission.setMinimumSize(size);
			//mission.setMaximumSize(size);
			mission.setOpaque(false);
			mission.setLayout( new BorderLayout() );

			JLabel cl = new JLabel( new ImageIcon(card) );
			cl.setBorder( BorderFactory.createEmptyBorder(1,3,1,3) );

			mission.add( cl , BorderLayout.WEST );

			JTextArea text = new JTextArea(a);
			text.setLineWrap(true);
			text.setWrapStyleWord(true);
			text.setEditable(false);
                        text.setBorder(null); // for nimbus
                        text.setBackground(new Color(0x00000000, true)); // for numbus setting null does not work
                        text.setForeground(Color.BLACK); // for gtk dark theme

			text.setOpaque(false);

			//JScrollPane sp = new JScrollPane(text);
			//sp.setBorder(null);

			//sp.setOpaque(false);
			//sp.getViewport().setOpaque(false);

			//mission.add( sp );
			mission.add( text );

			//mission.setBackground( new Color(255,255,255,100) );

			return mission;
	}

	public String getOptions() {
		return options;
	}

	public String getGameName() {
		return gamename.getText();
	}

	public int getNumberOfHumanPlayers() {
		return ((Integer)human.getValue()).intValue();
	}

	public void paintComponent(Graphics g) {

			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//			  destination		source
			GraphicsUtil.drawImage(g, newgame, 0, 0, this);


			GraphicsUtil.drawImage(g, game2.getSubimage(0,0,223,155), 41, 185, this);
			GraphicsUtil.drawImage(g, game2.getSubimage(25,155,169,127), 391, 325, this);

			g.setColor( Color.black );

			GraphicsUtil.drawString(g, resb.getString("newgame.label.map"), 55, 40);
			GraphicsUtil.drawString(g, resb.getString("newgame.missions"), 350, 40);

			GraphicsUtil.drawString(g, resb.getString("newgame.player.number"), 440, 275);

			//g.drawString( "Game Name:", 240, 545);

			GraphicsUtil.drawString(g, resb.getString("newgame.label.gametype"), 400, 365);
			GraphicsUtil.drawString(g, resb.getString("newgame.label.cardsoptions"), 515, 365);
	}

	public void reset() {
		options=null;
	}
        
	/**
	 * Actionlistener applies the correct command to the button pressed
	 * @param e The ActionEvent Object
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource()==start) {

			int a = ((Integer)human.getValue()).intValue();
			int b = ((Integer)aieasy.getValue()).intValue();
			int c = ((Integer)aiaverage.getValue()).intValue();
			int d = ((Integer)aihard.getValue()).intValue();

			int sum = a + b + c + d;

			if (sum >=2 && sum <= RiskGame.MAX_PLAYERS) {

                                int gameMode;
                                int cardsMode;
                            
                                if (domination.isSelected()) gameMode = RiskGame.MODE_DOMINATION;
                                else if (capital.isSelected()) gameMode = RiskGame.MODE_CAPITAL;
                                else gameMode = RiskGame.MODE_SECRET_MISSION; // if (mission.isSelected())

                                if (increasing.isSelected()) cardsMode = RiskGame.CARD_INCREASING_SET;
                                else if (fixed.isSelected()) cardsMode = RiskGame.CARD_FIXED_SET;
                                else cardsMode = RiskGame.CARD_ITALIANLIKE_SET; // if (italian.isSelected())

				options = OnlineUtil.createGameString(b, c, d, gameMode, cardsMode, AutoPlaceAll.isSelected(), recycle.isSelected(), riskmap.getID());

				dialog.setVisible(false);
			}
			else {

				JOptionPane.showMessageDialog(this, resb.getString("newgame.error.numberofplayers") , resb.getString("newgame.error.title"), JOptionPane.ERROR_MESSAGE );

			}

		}/*
		else if (e.getSource()==help) {

			try {
				Risk.openDocs( resb.getString("helpfiles.flash") );
			}
			catch(Exception er) {
				JOptionPane.showMessageDialog(this,"Unable to open manual: "+er.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
			}

		}*/
		else if (e.getSource()==cancel) {
			dialog.setVisible(false);
		}
		else if (e.getSource()==mission) {
			AutoPlaceAll.setEnabled(false);
		}
		else if (e.getSource()==domination) {
			AutoPlaceAll.setEnabled(true);
		}
		else if (e.getSource()==capital) {
			AutoPlaceAll.setEnabled(true);
		}
	}

    class RiskMapListCellRenderer extends DefaultListCellRenderer {
        private final Icon downloadIcon = new ImageIcon(getClass().getResource("/ms_download.png"));
        private final Icon downloadingIcon = new SpriteIcon(getClass().getResource("/ms_strip.png"), 1, 8);
        /**
         * as the renderer is not part of the view hierarchy, repainting it does nothing, so we need to pass the real component to paint in icons.
         */
        private JComponent component;
        private RiskMap map;
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component retValue = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBackground(isSelected ? new Color(255,255,255,100) : new Color(0,0,0,0));
            setForeground(Color.BLACK); // as we are in flash theme, and default color can be anything
            component = list;
            if (value instanceof RiskMap) {
                map = (RiskMap) value;
                setIcon(map.getIcon(50, 31, list));
            }
	    return retValue;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (downloading.contains(map)) {
                int x = getWidth() - downloadingIcon.getIconWidth() - GraphicsUtil.scale(5);
                int y = (getHeight() - downloadingIcon.getIconHeight()) / 2;
                g.setColor(Color.WHITE);
                g.fillOval(x, y, downloadingIcon.getIconWidth(), downloadingIcon.getIconHeight());
                downloadingIcon.paintIcon(component, g, x, y);
            }
            else if (!map.isLocalMap()) {
                downloadIcon.paintIcon(component, g, getWidth() - downloadIcon.getIconWidth() - 5, (getHeight() - downloadIcon.getIconHeight()) / 2);
            }
        }
    }
}
