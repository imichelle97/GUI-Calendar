/**Program Assignment #4: Simple GUI Calendar
 * Author: Michelle Luong
 * Copyright (C) 2017 Michelle Luong. All Rights Reserved.
 * Version: 1.01 5/11/2017
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CalendarView implements ChangeListener {
	
	private CalendarModel model;
	private DAYS[] listOfDays;
	private MONTHS[] listOfMonths;
	private int previousHighlight;
	private int maxDays;
	
	private JFrame frame = new JFrame("CALENDAR");
	private JPanel monthView = new JPanel();
	private JLabel monthLabel = new JLabel();
	private JButton create = new JButton("Create Event");
	private JButton deleteSpecified = new JButton("Delete Event Number");
	private JButton nextDay = new JButton("Next");
	private JButton previousDay = new JButton("Previous");
	private JTextPane dayTextPane = new JTextPane();
	private ArrayList<JButton> dayButton = new ArrayList<JButton>();
	
	/**
	 * Constructor for CalendarView
	 * @param model The model will store and update the calendar data
	 */
	public CalendarView(CalendarModel model) {
		this.model = model;
		listOfDays = DAYS.values();
		listOfMonths = MONTHS.values();
		maxDays = model.getMaxDays();
		monthView.setLayout(new GridLayout(0,7));
		dayTextPane.setPreferredSize(new Dimension(300, 150));
		dayTextPane.setEditable(true);
		
		createDayButtons();
		addBlankButtons();
		addDayButtons();
		highlightEvents();
		showDate(model.getSelectedDate());
		highlightSelectedDate(model.getSelectedDate() - 1);
		
		create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createEventDialog();
			}
		});
		
		deleteSpecified.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createDeleteDialog();
			}
		});
		
		//Button to go to the previous month
		JButton previousMonth = new JButton("<");
		previousMonth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.goPreviousMonth();
				create.setEnabled(false);
				nextDay.setEnabled(false);
				previousDay.setEnabled(false);
				dayTextPane.setText("");
			}
		});
		
		//Button to go to the next month		
		JButton nextMonth = new JButton(">");
		nextMonth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.goNextMonth();
				create.setEnabled(false);
				nextDay.setEnabled(false);
				previousDay.setEnabled(false);
				dayTextPane.setText("");
			}
		});

		//Button to delete an event on a specific date
		JButton delete = new JButton("Delete");
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.deleteEvent();
				create.setEnabled(false);
				unHighlightEvents();
			}
		}); 
		
		
		//Set up the labeling of the days of the week 
		JPanel monthContainer = new JPanel();
		monthContainer.setLayout(new BorderLayout());
		monthLabel.setText(listOfMonths[model.getMonth()] + " " + model.getYear());
		monthContainer.add(monthLabel, BorderLayout.NORTH);
		monthContainer.add(new JLabel("       S             M             T             W             T            F           S"), BorderLayout.CENTER);
		monthContainer.add(monthView, BorderLayout.SOUTH);
		
		JPanel dayView = new JPanel();
		dayView.setLayout(new GridLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		
		JScrollPane dayScrollPane = new JScrollPane(dayTextPane);
		dayScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		dayView.add(dayScrollPane, c);
		
		JPanel buttonPanel = new JPanel();
		nextDay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.goNextDay();
			}
		});
		
		previousDay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.goPreviousDay();
			}
		});
		
		buttonPanel.add(previousDay);
		buttonPanel.add(create);
		buttonPanel.add(nextDay);
		c.gridx = 0;
		c.gridy = 1;
		dayView.add(buttonPanel, c);
		
		JButton quit = new JButton("Quit");
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.save();
				System.exit(0);
			}
		});
		
		frame.add(previousMonth);
		frame.add(monthContainer);
		frame.add(nextMonth);
		frame.add(dayView);
		frame.add(delete);
		frame.add(deleteSpecified);
		frame.add(quit);
		frame.setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);		
		
	}
	
	public void stateChanged(ChangeEvent e) {
		if(model.hasMonthChanged()) {
			maxDays = model.getMaxDays();
			dayButton.clear();
			monthView.removeAll();
			monthLabel.setText(listOfMonths[model.getMonth()] + "" + model.getYear());
			createDayButtons();
			addBlankButtons();
			addDayButtons();
			highlightEvents();
			previousHighlight = -1;
			model.resetMonthChanged();
			frame.pack();
			frame.repaint();
		}
		else {
			showDate(model.getSelectedDate());
			highlightSelectedDate(model.getSelectedDate() - 1);
		}
	}
	
	/**
	 * Creates an event on the selectedDate
	 * User input
	 */
	public void createEventDialog() {
		final JDialog eventDialog = new JDialog();
		eventDialog.setTitle("Create Event");
		final JTextField eventText = new JTextField(30);
		final JTextField startTime = new JTextField(10);
		final JTextField endTime = new JTextField(10);
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (eventText.getText().isEmpty()) {
					return;
				}
				if ((!eventText.getText().isEmpty() && (startTime.getText().isEmpty() || endTime.getText().isEmpty()))
						|| startTime.getText().length() != 5
						|| endTime.getText().length() != 5
						|| !startTime.getText().matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")
						|| !endTime.getText().matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
					JDialog timeErrorDialog = new JDialog();
					timeErrorDialog.setLayout(new GridLayout(2, 0));
					timeErrorDialog.add(new JLabel("Please enter start and end time in format XX:XX."));
					JButton ok = new JButton("Okay");
					ok.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							timeErrorDialog.dispose();
						}
					});
					timeErrorDialog.add(ok);
					timeErrorDialog.pack();
					timeErrorDialog.setVisible(true);
			}
				else if(!eventText.getText().equals("")) {
					if(model.hasEventConflict(startTime.getText(), endTime.getText())) {
						JDialog conflictDialog = new JDialog();
						conflictDialog.setLayout(new GridLayout(2,0));
						conflictDialog.add(new JLabel("Time Conflict!"));
						JButton ok = new JButton("Okay");
						ok.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								conflictDialog.dispose();
							}
						});
						conflictDialog.add(ok);
						conflictDialog.pack();
						conflictDialog.setVisible(true);
					} else {
						eventDialog.dispose();
						model.createEvent(eventText.getText(), startTime.getText(), endTime.getText());
						showDate(model.getSelectedDate());
						highlightEvents();
					}
				}
			}
		});
		eventDialog.setLayout(new GridBagLayout());
		JLabel date = new JLabel();
		date.setText(model.getMonth() + 1 + "/" + model.getSelectedDate() + "/" + model.getYear());
		date.setBorder(BorderFactory.createEmptyBorder());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		eventDialog.add(date, c);
		c.gridy = 1;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		eventDialog.add(new JLabel("Event"), c);
		c.gridy = 2;
		eventDialog.add(eventText, c);
		c.gridy = 3;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_START;
		eventDialog.add(new JLabel("Time Start"), c);
		c.anchor = GridBagConstraints.CENTER;
		eventDialog.add(new JLabel("Time End"), c);
		c.gridy = 4;
		c.anchor = GridBagConstraints.LINE_START;
		eventDialog.add(startTime, c);
		c.anchor = GridBagConstraints.CENTER;
		eventDialog.add(endTime, c);
		c.anchor = GridBagConstraints.LINE_END;
		eventDialog.add(save, c);
		eventDialog.pack();
		eventDialog.setVisible(true);
	}
	
	public void createDeleteDialog() {
		final JDialog deleteDialog = new JDialog();
		deleteDialog.setTitle("Delete Event Number");
		final JTextField eventNumber = new JTextField(30);
		JButton save = new JButton("Save");
		
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				model.deleteSpecificEvent(eventNumber.getText());
				create.setEnabled(false);
			}
		});
		
		deleteDialog.setLayout(new GridBagLayout());
		JLabel date = new JLabel();
		date.setText(model.getMonth() + 1 + "/" + model.getSelectedDate() + "/" + model.getYear());
		date.setBorder(BorderFactory.createEmptyBorder());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		deleteDialog.add(date, c);
		c.gridy = 1;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		deleteDialog.add(new JLabel("Event Number"), c);
		c.gridy = 2;
		deleteDialog.add(eventNumber, c);
		c.gridy = 3;
		c.weightx = 0.0;
		
		deleteDialog.add(save, c);
		deleteDialog.pack();
		deleteDialog.setVisible(true);
		
	}

	/**
	 * Shows the selected date and events on that date.
	 * @param d The selected date
	 */
	private void showDate(final int d) {
		model.setSelectedDate(d);
		String dayOfWeek = listOfDays[model.getDayOfWeek(d) - 1] + "";
		String date = (model.getMonth() + 1) + "/" + d + "/" + model.getYear();
		String events = "";
		if (model.hasEvent(date)) {
			events += model.getEvents(date);
		}
		dayTextPane.setText(dayOfWeek + " " + date + "\n" + events);
	}

	/**
	 * Highlights the currently selected date.
	 * @param d the currently selected date
	 */
	private void highlightSelectedDate(int d) {
		Border border = new LineBorder(Color.BLUE, 2);
		dayButton.get(d).setBorder(border);
		if (previousHighlight != -1) {
			dayButton.get(previousHighlight).setBorder(new JButton().getBorder());
		}
		previousHighlight = d;
	}

	/**
	 * Highlights days containing events.
	 */
	private void highlightEvents() {
		for (int i = 1; i <= maxDays; i++) {
			if (model.hasEvent((model.getMonth() + 1) + "/" + i + "/" + model.getYear())) {
				dayButton.get(i - 1).setBackground(Color.YELLOW);
			}
		}
	}
	
	/**
	 * When event is deleted, unhighlight that day
	 */
	private void unHighlightEvents() {
		for (int i = 1; i <= maxDays; i++) {
			if (model.hasEvent((model.getMonth() + 1) + "/" + i + "/" + model.getYear())) {
				dayButton.get(i - 1).setBackground(Color.WHITE);
			}
		}
	}
	
	/**
	 * Creates buttons representing days of the current month and adds them to an array list
	 *	The day buttons are clickable 
	 */
	private void createDayButtons() {
		for (int i = 1; i <= maxDays; i++) {
			final int d = i;
			JButton day = new JButton(Integer.toString(d));
			day.setBackground(Color.WHITE);
	
			day.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent arg0) {
					showDate(d);
					highlightSelectedDate(d - 1);
					create.setEnabled(true);
					nextDay.setEnabled(true);
					previousDay.setEnabled(true);
				}
			});
			dayButton.add(day);
		}
	}

	/**
	 * Adds the buttons representing the days of the month to the panel.
	 */
	private void addDayButtons() {
		for (JButton d : dayButton) {
			monthView.add(d);
		}
	}

	/**
	 * Adds filler buttons before the start of the month to align calendar.
	 */
	private void addBlankButtons() {
		for (int j = 1; j < model.getDayOfWeek(1); j++) {
			JButton blank = new JButton();
			blank.setEnabled(false);
			monthView.add(blank);
		}
	}
}
					