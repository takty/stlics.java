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
public class RandomBinaryDialog extends JDialog {

	private int exitCode_;

    public RandomBinaryDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

	public int getVariableCount() {
		return (Integer)spVariableCount.getValue();
	}

	public double getDensity() {
		return (Double)spDensity.getValue();
	}

	public double getAverageTightness() {
		return (Double)spTightness.getValue();
	}

	public int getDomainSize() {
		return (Integer)spDomainSize.getValue();
	}

	public int showDialog() {
        exitCode_ = 0;
		setVisible(true);
		return exitCode_;
	}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buCancel = new JButton();
        buOk = new JButton();
        jLabel4 = new JLabel();
        spVariableCount = new JSpinner();
        jLabel5 = new JLabel();
        spDensity = new JSpinner();
        jLabel6 = new JLabel();
        spTightness = new JSpinner();
        jLabel7 = new JLabel();
        spDomainSize = new JSpinner();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Random Binary");
        setLocationByPlatform(true);
        setModal(true);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

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

        jLabel4.setText("Number of variables:");

        spVariableCount.setModel(new SpinnerNumberModel(10, 1, 100, 1));

        jLabel5.setText("Density:");

        spDensity.setModel(new SpinnerNumberModel(0.5d, 0.1d, 1.0d, 0.1d));

        jLabel6.setText("Average tightness:");

        spTightness.setModel(new SpinnerNumberModel(0.5d, 0.1d, 1.0d, 0.1d));

        jLabel7.setText("Domain size:");

        spDomainSize.setModel(new SpinnerNumberModel(10, 1, 100, 1));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buCancel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(buOk))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(45, 45, 45)
                                .addComponent(spDomainSize, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel5))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
                                    .addComponent(spDensity, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                                    .addComponent(spVariableCount, Alignment.LEADING)
                                    .addComponent(spTightness))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {buCancel, buOk});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(spVariableCount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(spDensity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(spTightness, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(spDomainSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(buCancel)
                    .addComponent(buOk))
                .addContainerGap())
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
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JSpinner spDensity;
    private JSpinner spDomainSize;
    private JSpinner spTightness;
    private JSpinner spVariableCount;
    // End of variables declaration//GEN-END:variables

}
