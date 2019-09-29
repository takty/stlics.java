

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * N-1クイーン問題の実装サンプルの設定ダイアログ・ボックスです．
 * @author Takuto Yanagida
 * @version 2012/11/22
 */
public class N_queensDialog extends JDialog {

	private int exitCode_;

    public N_queensDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

	public int getQueenSize() {
		return (Integer)spQueens.getValue();
	}

	public int showDialog() {
        exitCode_ = 0;
		setVisible(true);
		return exitCode_;
	}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        laQueens = new JLabel();
        spQueens = new JSpinner();
        buCancel = new JButton();
        buOk = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("N Queens");
        setLocationByPlatform(true);
        setModal(true);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        laQueens.setText("Number of queens:");

        spQueens.setModel(new SpinnerNumberModel(Integer.valueOf(8), Integer.valueOf(1), null, Integer.valueOf(1)));

        buCancel.setText("Cancel");
        buCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buCancelActionPerformed(evt);
            }
        });

        buOk.setText("OK");
        buOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buOkActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buCancel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(buOk))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(laQueens)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(spQueens, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {buCancel, buOk});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(laQueens)
                    .addComponent(spQueens, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(buCancel)
                    .addComponent(buOk))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void buOkActionPerformed(ActionEvent evt) {//GEN-FIRST:event_buOkActionPerformed
        exitCode_ = 1;
        setVisible(false);
	}//GEN-LAST:event_buOkActionPerformed

	private void buCancelActionPerformed(ActionEvent evt) {//GEN-FIRST:event_buCancelActionPerformed
        exitCode_ = 0;
        setVisible(false);
	}//GEN-LAST:event_buCancelActionPerformed

	private void formWindowActivated(WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
		getRootPane().setDefaultButton(buOk);
	}//GEN-LAST:event_formWindowActivated

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton buCancel;
    private JButton buOk;
    private JLabel laQueens;
    private JSpinner spQueens;
    // End of variables declaration//GEN-END:variables

}
