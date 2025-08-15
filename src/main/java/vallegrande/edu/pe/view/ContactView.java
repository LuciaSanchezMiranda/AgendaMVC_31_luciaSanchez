package vallegrande.edu.pe.view;

import vallegrande.edu.pe.controller.ContactController;
import vallegrande.edu.pe.model.Contact;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Tema azul/blanco, botones con iconos, tipografía moderna y notificaciones visuales.
 */
public class ContactView extends JFrame {
    private final ContactController controller;
    private DefaultTableModel tableModel;
    private JTable table;

    public ContactView(ContactController controller) {
        super("Agenda MVC Swing - Vallegrande");
        this.controller = controller;
        initUI();
        loadContacts();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Fuente base para todo
        Font baseFont = new Font("Segoe UI", Font.PLAIN, 16);
        UIManager.put("OptionPane.messageFont", baseFont);
        UIManager.put("OptionPane.buttonFont", baseFont);

        // Panel principal con márgenes
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(new Color(230, 240, 255)); // azul muy claro

        setContentPane(contentPanel);

        // Encabezado con mensaje de bienvenida
        JLabel headerLabel = new JLabel("BIENVENIDO A TU AGENDA DE CONTACTOS", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(0, 102, 204));
        headerLabel.setBorder(new EmptyBorder(10, 0, 20, 0));
        contentPanel.add(headerLabel, BorderLayout.NORTH);

        // Tabla
        // Tabla
        tableModel = new DefaultTableModel(new String[]{"ID", "Nombre", "Email", "Teléfono"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel) {
            // Para que no pinte el fondo entero y podamos personalizar hover
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    comp.setBackground(new Color(0, 123, 255)); // seleccionado
                    comp.setForeground(Color.WHITE);
                } else if (row == getSelectedRow()) {
                    comp.setBackground(new Color(220, 235, 255)); // hover suave
                    comp.setForeground(Color.BLACK);
                } else {
                    comp.setBackground(Color.WHITE); // normal
                    comp.setForeground(Color.DARK_GRAY);
                }
                return comp;
            }
        };
        table.setFont(baseFont);
        table.setRowHeight(30);
        table.getTableHeader().setFont(baseFont.deriveFont(Font.BOLD, 18f));
        table.setSelectionBackground(new Color(0, 123, 255));
        table.setSelectionForeground(Color.WHITE);

         // Scroll con bordes redondeados y sombra
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 204), 2, true), // azul, bordes redondeados
                BorderFactory.createEmptyBorder(5, 5, 5, 5) // margen interno
        ));
        // Simulación de sombra usando MatteBorder
        scrollPane.setViewportBorder(BorderFactory.createMatteBorder(1, 1, 5, 5, new Color(180, 180, 180)));

        contentPanel.add(scrollPane, BorderLayout.CENTER);


        // Panel de botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonsPanel.setBackground(Color.WHITE);

        JButton addBtn = new JButton("Agregar");
        addBtn.setIcon(new ImageIcon(getClass().getResource("/icons/guardar.png")));
        styleButton(addBtn, new Color(0, 123, 255), null);

        JButton deleteBtn = new JButton("Eliminar");
        deleteBtn.setIcon(new ImageIcon(getClass().getResource("/icons/eliminar.png")));
        styleButton(deleteBtn, new Color(220, 53, 69), null);


        buttonsPanel.add(addBtn);
        buttonsPanel.add(deleteBtn);
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Eventos
        addBtn.addActionListener(e -> {
            showAddContactDialog();
        });

        deleteBtn.addActionListener(e -> {
            deleteSelectedContact();
        });
    }





    /**
     * Estilo para botones con icono, hover y bordes suaves.
     */
    /**
     * Estilo para botones con icono, hover suave y bordes redondeados.
     */
    private void styleButton(JButton button, Color baseColor, String icon) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Texto un poco más grande
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);

        // Tamaño fijo un poco más grande
        button.setPreferredSize(new Dimension(160, 45));

        // Bordes redondeados usando LineBorder con grosor 2
        button.setBorder(BorderFactory.createLineBorder(baseColor.darker(), 2, true));

        // Quitar el área pintada para que respete el borde redondeado
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        // Cursor tipo mano
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Solo agregar emoji al texto si no es null
        if (icon != null && !icon.isEmpty()) {
            button.setText(icon + " " + button.getText());
        }



        // Hover suave
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });
    }






    private void loadContacts() {
        tableModel.setRowCount(0);
        List<Contact> contacts = controller.list();
        for (Contact c : contacts) {
            tableModel.addRow(new Object[]{c.id(), c.name(), c.email(), c.phone()});
        }
    }

    private void showAddContactDialog() {
        AddContactDialog dialog = new AddContactDialog(this, controller);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            loadContacts();
            showToast("Contacto agregado con éxito", true);
        }
    }

    private void deleteSelectedContact() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un contacto para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar este contacto?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.delete(id);
            loadContacts();
            showToast("Contacto eliminado", false);
        }
    }

    /**
     * Notificación visual tipo "toast".
     */
    private void showToast(String message, boolean success) {
        JDialog toast = new JDialog(this, false);
        toast.setUndecorated(true);

        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(success ? new Color(40, 167, 69) : new Color(220, 53, 69));
        lbl.setBorder(new EmptyBorder(10, 20, 10, 20));

        toast.add(lbl);
        toast.pack();
        toast.setLocationRelativeTo(this);

        new Timer(1500, e -> toast.dispose()).start();
        toast.setVisible(true);
    }
}
