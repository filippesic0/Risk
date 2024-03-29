// Yura Mamyrin

package net.yura.domination.ui.swinggui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;
import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.OnlineUtil;
import net.yura.domination.engine.Risk;
import net.yura.domination.guishared.RiskUIUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.guishared.PicturePanel;
import net.yura.domination.mapstore.Map;
import net.yura.domination.mapstore.MapChooser;
import net.yura.domination.mapstore.MapUpdateService;
import net.yura.domination.tools.mapeditor.MapsTools;
import net.yura.domination.ui.flashgui.MainMenu;
import net.yura.mobile.util.Url;
import net.yura.swing.JTable;

/**
 * @author Yura Mamyrin
 */
public class TestPanel extends JPanel implements ActionListener, SwingGUITab {

	private Risk myrisk;

	private JToolBar toolbar;

	private AbstractTableModel countriesModel;
	private AbstractTableModel continentsModel;
	private AbstractTableModel cardsModel,cardsModel2;
	private AbstractTableModel playersModel;
        private AbstractTableModel gameInfo;
        private AbstractTableModel commands;

	private PicturePanel pp;

	public TestPanel(Risk r,PicturePanel p) {

		myrisk = r;
		pp=p;

		setName( "Testing" );

		setOpaque(false);

		toolbar = new JToolBar();

		toolbar.setRollover(true);
		toolbar.setFloatable(false);

		JButton refresh = new JButton("Refresh");
		refresh.setActionCommand("refresh");
		refresh.addActionListener(this);
		toolbar.add(refresh);

		JButton allcards = new JButton("All Cards");
		allcards.setActionCommand("allcards");
		allcards.addActionListener(this);
		toolbar.add(allcards);

		toolbar.addSeparator();

		JButton flash = new JButton("Run FlashGUI with current backend");
		flash.setActionCommand("flash");
		flash.addActionListener(this);
		toolbar.add(flash);

                toolbar.addSeparator();

		JButton mapServerNameChack = new JButton("Check MapServer");
		mapServerNameChack.setActionCommand("checkMapServer");
		mapServerNameChack.addActionListener(this);
		toolbar.add(mapServerNameChack);

		countriesModel = new AbstractTableModel() {

			private final String[] columnNames = { "Color/No.","ID","Name","x","y","Continent","Owner","Armies","No. Neighbours","in","con" };

			public int getColumnCount() {
				return columnNames.length;
			}
                        
                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                            if ("Armies".equals(columnNames[columnIndex])) {
                                return true;
                            }
                            return false;
                        }

			public int getRowCount() {

				RiskGame game = myrisk.getGame();

				if (game != null) {

					Country[] countries = game.getCountries();

					if (countries != null) {

						return countries.length;
					}
				}

				return 0;
  			}

			public String getColumnName(int col) {
				return columnNames[col];
			}

			public Object getValueAt(int row, int col) {

				Country country = myrisk.getGame().getCountries()[row];

				switch(col) {
					case 0: return new Integer( country.getColor() );
					case 1: return country.getIdString();
					case 2: return country.getName();
					case 3: return new Integer( country.getX() );
					case 4: return new Integer( country.getY() );
					case 5: return country.getContinent();
					case 6: return country.getOwner();
					case 7: return new Integer( country.getArmies() );
					case 8: {
                                            List neighbours = country.getNeighbours();
                                            if (neighbours==null) return null;
                                            return new Integer( neighbours.size() );
                                        }
                                        case 9: {
                                            List neighbours = country.getIncomingNeighbours();
                                            if (neighbours==null) return null;
                                            return new Integer( neighbours.size() );
                                        }
                                        case 10: {
                                            List neighbours = country.getCrossContinentNeighbours();
                                            if (neighbours==null) return null;
                                            return new Integer( neighbours.size() );
                                        }
					default: throw new RuntimeException();
				}
			}

                        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                            Country country = myrisk.getGame().getCountries()[rowIndex];
                            switch(columnIndex) {
                                case 7: int armies = country.getArmies();
                                        int newArmies = Integer.parseInt(aValue.toString());
                                        if (newArmies > armies) {
                                            country.addArmies(newArmies - armies);
                                        }
                                        else {
                                            country.removeArmies(armies - newArmies);
                                        }
                                        break;
                            }
                        }
		};




		continentsModel = new AbstractTableModel() {

			private final String[] columnNames = { "No.", "ID","Name", "Army Value", "No. Countries","Color" };

			public int getColumnCount() {
				return columnNames.length;
			}

			public int getRowCount() {

				RiskGame game = myrisk.getGame();

				if (game != null) {

					Continent[] continents = game.getContinents();

					if (continents != null) {

						return continents.length;
					}
				}

				return 0;
  			}

			public String getColumnName(int col) {
				return columnNames[col];
			}

			public Object getValueAt(int row, int col) {

				Continent continent = myrisk.getGame().getContinents()[row];

				switch(col) {

					case 0: return new Integer( row+1 );
					case 1: return continent.getIdString();
					case 2: return continent.getName();
					case 3: return new Integer( continent.getArmyValue() );
					case 4: return new Integer( continent.getTerritoriesContained().size() );
					case 5: return ColorUtil.getStringForColor( continent.getColor() );
					default: throw new RuntimeException();
				}
			}
		};


		cardsModel = new CardsTableModel() {
                    List getCards() {
                        RiskGame game = myrisk.getGame();
                        if (game!=null) {
                            List l = game.getCards();
                            return l==null?Collections.EMPTY_LIST:l;
                        }
                        return Collections.EMPTY_LIST;
                    }
                };

                cardsModel2 = new CardsTableModel() {
                    List getCards() {
                        RiskGame game = myrisk.getGame();
                        if (game!=null) {
                            List l = game.getUsedCards();
                            return l==null?Collections.EMPTY_LIST:l;
                        }
                        return Collections.EMPTY_LIST;
                    }
                };

		playersModel = new AbstractTableModel() {

			private final String[] columnNames = { "Name", "Color", "Type", "Extra Armies", "No. Cards", "No. Countries", "No. Player Eliminated", "Capital", "Mission", "Address", "autodefend","autoendgo"};

			public int getColumnCount() {
				return columnNames.length;
			}

			public int getRowCount() {
				RiskGame game = myrisk.getGame();

				if (game != null) {
					List players = game.getPlayers();

					if (players != null) {
						return players.size();
					}
				}
				return 0;
  			}

			public String getColumnName(int col) {
				return columnNames[col];
			}

			public Object getValueAt(int row, int col) {

				Player player = (Player)myrisk.getGame().getPlayers().elementAt(row);

				switch(col) {

					case 0: return player.getName();
					case 1: return ColorUtil.getStringForColor( player.getColor() );
					case 2: return myrisk.getType(player.getType());
					case 3: return new Integer( player.getExtraArmies() );
					case 4: return new Integer( player.getCards().size() );
					case 5: return new Integer( player.getNoTerritoriesOwned() );
					case 6: return new Integer( player.getPlayersEliminated().size() );
					case 7: return player.getCapital();
					case 8: return player.getMission();
					case 9: return player.getAddress();
					case 10: return new Boolean( player.getAutoDefend() );
					case 11: return new Boolean( player.getAutoEndGo() );
					default: throw new RuntimeException();
				}
			}

                        @Override
                        public boolean isCellEditable(int row, int col) {
                            switch (col) {
                                case 0:// name
                                case 2:// type
                                case 9:// address
                                    return true;
                                default:
                                    return false;
                            }
                        }

                        @Override
                        public void setValueAt(Object aValue, int row, int col) {
                            Player player = (Player)myrisk.getGame().getPlayers().elementAt(row);

                            try {
                                String name = col==0?String.valueOf(aValue):player.getName();
                                if (name.equals("")) {
                                    throw new IllegalArgumentException("no empty name");
                                }
                                int type = col==2?myrisk.getType(String.valueOf(aValue)):player.getType();
                                if (type == -1) {
                                    throw new IllegalArgumentException("bad type "+aValue);
                                }
                                String address = col==9?String.valueOf(aValue):player.getAddress();
                                if (address.equals("")) {
                                    throw new IllegalArgumentException("no empty address");
                                }

                                HashMap map = new HashMap();
                                map.put("oldName", player.getName());
                                map.put("newName", name);
                                map.put("newType", type);
                                map.put("newAddress", address);
                                
                                // this will only change the local state, there is no way to send this out to all in a network game
                                myrisk.parserFromNetwork("RENAME "+Url.toQueryString(RiskUtil.asHashtable(map)) );
                            }
                            catch (Exception ex) {
                                System.out.println("error "+ex);
                            }
                        }
		};


                gameInfo = new ObjectTableModel() {
                    public Object getObject() {
                        return myrisk.getGame();
                    }
		};

                commands = new AbstractTableModel() {

			private final String[] columnNames = { "No", "Player", "Command"};

			public int getColumnCount() {
				return columnNames.length;
			}

			public int getRowCount() {
				RiskGame game = myrisk.getGame();
				if (game != null) {
					List players = game.getCommands();
					if (players != null) {
						return players.size();
					}
				}
				return 0;
  			}

			public String getColumnName(int col) {
				return columnNames[col];
			}

			public Object getValueAt(int row, int col) {
				String command = String.valueOf(myrisk.getGame().getCommands().elementAt(row));
				switch(col) {
					case 0: return String.valueOf(row);
                                        case 1: {
                                            int space = command.indexOf(" ");
                                            if (space > 0) {
                                                String address = command.substring(0, space);
                                                List<Player> players = myrisk.getGame().getPlayers();
                                                for (Player player : players) {
                                                    if (player.getAddress().equals(address)) {
                                                        return player;
                                                    }
                                                }
                                            }
                                            return null;
                                        }
					case 2: return command;
					default: throw new RuntimeException();
				}
			}
		};


		JTabbedPane views = new JTabbedPane();

		views.add( "Countries" , new JScrollPane(new JTable(countriesModel)) );
		views.add( "Continents" , new JScrollPane(new JTable(continentsModel)) );
		views.add( "Cards" , new JScrollPane(new JTable(cardsModel)) );
                views.add( "Spent Cards" , new JScrollPane(new JTable(cardsModel2)) );
		views.add( "Players" , new JScrollPane(new JTable(playersModel)) );
                views.add( "Game" , new JScrollPane(new JTable(gameInfo)) );

                JTable commandsTable = new JTable(commands);
                commandsTable.setCellSelectionEnabled(true);

                views.add( "Commands" , new JScrollPane(commandsTable) );

		setLayout( new BorderLayout() );
		add( views );

	}

        abstract class ObjectTableModel extends AbstractTableModel {

                private final String[] columnNames = { "Name", "Type", "Value" };
                private Field[] fields;

                public int getColumnCount() {
                        return columnNames.length;
                }

                public int getRowCount() {
                        Object game = getObject();
                        if (game != null) {
                            if (fields==null) {
                                Field[] fs = game.getClass().getDeclaredFields();
                                List result = new ArrayList();
                                for (int c=0;c<fs.length;c++) {
                                    if (!java.lang.reflect.Modifier.isStatic(fs[c].getModifiers())) {
                                        try {
                                            fs[c].setAccessible(true);
                                            result.add(fs[c]);
                                        }
                                        catch (Exception ex) { // security
                                         System.out.println("can not setAccessible "+fs[c]+" "+ex);
                                        }
                                    }
                                }
                                fields = (Field[])result.toArray(new Field[result.size()]);
                            }
                            return fields.length;
                        }
                        return 0;
                }

                public String getColumnName(int col) {
                        return columnNames[col];
                }

                public Object getValueAt(int row, int col) {
                        Object game = getObject();
                        switch(col) {
                                case 0: return fields[row].getName();
                                case 1: return fields[row].getType().getName();
                                case 2: {
                                    try {
                                        return String.valueOf( fields[row].get(game) );
                                    }
                                    catch (Exception ex){
                                        return ex.toString();
                                    }
                                }
                                default: throw new IllegalArgumentException("bad col " + col);
                        }
                }

                public abstract Object getObject();

        }

        abstract class CardsTableModel extends AbstractTableModel {

                private final String[] columnNames = { "No.", "Type", "Country" };

                abstract List getCards();

                public int getColumnCount() {
                        return columnNames.length;
                }

                public int getRowCount() {
                        return getCards().size();
                }

                public String getColumnName(int col) {
                        return columnNames[col];
                }

                public Object getValueAt(int row, int col) {
                        Card card = (Card)getCards().get(row);
                        switch(col) {
                                case 0: return new Integer( row+1 );
                                case 1: return card.getName();
                                case 2: return card.getCountry();
                                default: throw new RuntimeException();
                        }
                }
        }

	public void actionPerformed(ActionEvent a) {

                String command = a.getActionCommand();

		if ("refresh".equals(command)) {

			countriesModel.fireTableDataChanged();
			continentsModel.fireTableDataChanged();
			cardsModel.fireTableDataChanged();
                        cardsModel2.fireTableDataChanged();
			playersModel.fireTableDataChanged();
                        gameInfo.fireTableDataChanged();
                        commands.fireTableDataChanged();

			repaint();
		}
		else if ("flash".equals(command)) {
			MainMenu.newMainMenuFrame( myrisk, JFrame.DISPOSE_ON_CLOSE );
		}
		else if ("allcards".equals(command)) {

			if (myrisk.getGame() != null && myrisk.getGame().getCards() != null) {

				Frame frame = RiskUIUtil.findParentFrame(this);

				CardsDialog cardsDialog = new CardsDialog(frame, pp, false, myrisk);
				Dimension frameSize = frame.getSize();
				Dimension aboutSize = cardsDialog.getPreferredSize();
				int x = frame.getLocation().x + (frameSize.width - aboutSize.width) / 2;
				int y = frame.getLocation().y + (frameSize.height - aboutSize.height) / 2;
				if (x < 0) x = 0;
				if (y < 0) y = 0;
				cardsDialog.setLocation(x, y);

				cardsDialog.populate(myrisk.getGame().getCards(), false);

				cardsDialog.setVisible(true);
			}
		}
                else if ("checkMapServer".equals(command)) {
                    // get all maps
                    List<Map> maps = MapUpdateService.getMaps(MapChooser.MAP_PAGE,Collections.EMPTY_LIST);
                    if (maps.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "ERROR: No maps found on server!");
                        return;
                    }

                    Set<String> ids = new HashSet();
                    Set<String> errors = new HashSet();
                    String longestMapName = RiskGame.getDefaultMap();
                    for (Map map: maps) {
                        String filename = MapChooser.getFileUID( map.getMapUrl() );
                        if (filename.length() > longestMapName.length()) {
                            longestMapName = filename;
                        }

                        String fileUID = RiskUtil.replaceAll(filename," ","").toLowerCase();
                        if (!MapsTools.isValidName(filename) || ids.contains(fileUID)) {
                            errors.add(filename);
                        }
                        else {
                            ids.add(fileUID);
                        }
                    }

                    String options = OnlineUtil.createGameString(1, 1, 1, RiskGame.MODE_DOMINATION, RiskGame.CARD_ITALIANLIKE_SET, true, true, longestMapName);
                    String info = "\nLongest map name: \"" + longestMapName + "\" (" + longestMapName.length() + ") max options lenght: " + options.length();
                    // current max allowed by Lobby DB is 200 {@link net.yura.lobby.database.GameRoom#getOptions()}

                    if (errors.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No errors found." + info);
                    }
                    else {
                        JOptionPane.showMessageDialog(this, "Error found with map: " + errors + info);
                    }
                }
		else {
			throw new RuntimeException("TestTab: unknown command found: "+command);
		}
	}

	public JToolBar getToolBar() {
		return toolbar;
	}

	public JMenu getMenu() {
		return null;
	}
}
