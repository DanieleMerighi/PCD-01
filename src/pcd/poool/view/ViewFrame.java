package pcd.poool.view;

import pcd.poool.controller.ArrowKeyCmd;
import pcd.poool.controller.Cmd;
import pcd.poool.model.Direction;
import pcd.poool.util.BoundedBuffer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ViewFrame extends JFrame implements KeyListener {

	private final VisualiserPanel panel;
	private final ViewModel viewModel;
	private final BoundedBuffer<Cmd> cmdBuffer;

	public ViewFrame(ViewModel viewModel, BoundedBuffer<Cmd> cmdBuffer, int w, int h){
		this.viewModel = viewModel;
		this.cmdBuffer = cmdBuffer;
		setTitle("Poool");
		setResizable(false);
		panel = new VisualiserPanel(w,h);
		getContentPane().add(panel);
		pack();

		this.addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		requestFocusInWindow();

		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent ev){
				System.exit(-1);
			}
			public void windowClosed(WindowEvent ev){
				System.exit(-1);
			}
		});
	}

	public void render(){
		panel.repaint();
	}

	public class VisualiserPanel extends JPanel {
		private final int ox;
		private final int oy;
		private final int delta;

		public VisualiserPanel(int w, int h){
			setPreferredSize(new Dimension(w,h));
			ox = w/2;
			oy = h/2;
			delta = Math.min(ox, oy);
		}

		public void paint(Graphics g){
			Graphics2D g2 = (Graphics2D) g;

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g2.clearRect(0,0,this.getWidth(),this.getHeight());

			g2.setColor(Color.LIGHT_GRAY);
			g2.setStroke(new BasicStroke(1));
			g2.drawLine(ox,0,ox,oy*2);
			g2.drawLine(0,oy,ox*2,oy);
			g2.setColor(Color.BLACK);

			for (var hole: viewModel.getHoles()) {
				var p = hole.pos();
				int x0 = (int)(ox + p.x()*delta);
				int y0 = (int)(oy - p.y()*delta);
				int radiusX = (int)(hole.radius()*delta);
				int radiusY = (int)(hole.radius()*delta);
				g2.fillOval(x0 - radiusX,y0 - radiusY,radiusX * 2,radiusY * 2);
			}

			g2.setStroke(new BasicStroke(1));
			for (var b: viewModel.getBalls()) {
				var p = b.pos();
				int x0 = (int)(ox + p.x()*delta);
				int y0 = (int)(oy - p.y()*delta);
				int radiusX = (int)(b.radius()*delta);
				int radiusY = (int)(b.radius()*delta);
				g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
			}

			g2.setStroke(new BasicStroke(3));
			var pb = viewModel.getPlayerBall();
			if (pb != null) {
				var p1 = pb.pos();
				int x0 = (int)(ox + p1.x()*delta);
				int y0 = (int)(oy - p1.y()*delta);
				int radiusX = (int)(pb.radius()*delta);
				int radiusY = (int)(pb.radius()*delta);
				g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
			}

			g2.setStroke(new BasicStroke(1));
			g2.drawString("Num small balls: " + viewModel.getBalls().size(), 20, 2 * oy - 60);
			g2.drawString("Frame per sec: " + viewModel.getFramePerSec(), 20, 2 * oy - 40);
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getExtendedKeyCode() == KeyEvent.VK_UP) {
			cmdBuffer.put(new ArrowKeyCmd(Direction.UP));
		} else if (e.getExtendedKeyCode() == KeyEvent.VK_LEFT) {
			cmdBuffer.put(new ArrowKeyCmd(Direction.LEFT));
		} else if (e.getExtendedKeyCode() == KeyEvent.VK_DOWN) {
			cmdBuffer.put(new ArrowKeyCmd(Direction.DOWN));
		} else if (e.getExtendedKeyCode() == KeyEvent.VK_RIGHT) {
			cmdBuffer.put(new ArrowKeyCmd(Direction.RIGHT));
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
}
