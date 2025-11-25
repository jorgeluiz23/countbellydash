package br.com.countbellydash.dao;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos; 
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Main extends Application {

    // Simulação do Service e Dados (Mantenha seu código real aqui)
    private final DashboardService service = new DashboardService();
    private Stage primaryStage; 
    private BorderPane rootDashboard;
    private double zoomScale = 1.0; 

    // Constantes de Login
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "123123";
    
    // Constantes de Zoom
    private static final double ZOOM_STEP = 0.1;
    private static final double MAX_ZOOM = 1.5;
    private static final double MIN_ZOOM = 0.8;
    
    // Formatação da Data/Hora
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Countbelly Dashboard - Login");
        stage.setFullScreen(false); 
        showLoginScreen(stage);
    }
    
    // --- LÓGICA DE LOGIN ---

    private void showLoginScreen(Stage stage) {
        Label title = createTitleLabel("Acesso ao Dash do CountBelly");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Usuário");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Senha");
        Button loginButton = new Button("Acessar");
        Label messageLabel = new Label("");
        
        usernameField.setMaxWidth(250);
        passwordField.setMaxWidth(250);
        loginButton.getStyleClass().add("login-button");
        messageLabel.getStyleClass().add("metric-danger");

        VBox loginLayout = new VBox(20);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(50));
        loginLayout.getChildren().addAll(
            title, usernameField, passwordField, loginButton, messageLabel
        );
        loginLayout.getStyleClass().add("login-panel");
        
        loginButton.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            
            if (USERNAME.equals(user) && PASSWORD.equals(pass)) {
                showDashboard(stage);
            } else {
                messageLabel.setText("Usuário ou senha incorretos.");
            }
        });

        Scene loginScene = new Scene(loginLayout, 500, 350);
        String cssPath = getClass().getResource("/dashboard.css").toExternalForm();
        loginScene.getStylesheets().add(cssPath);
        
        stage.setScene(loginScene); 
        stage.setMinHeight(350);
        stage.setMinWidth(500);
        stage.show();
    }


    // --- LÓGICA DO DASHBOARD ---

    private void showDashboard(Stage stage) {
        
        rootDashboard = new BorderPane();
        rootDashboard.setPadding(new Insets(20));
        
        String cssPath = getClass().getResource("/dashboard.css").toExternalForm();
        Scene scene = new Scene(rootDashboard, 1200, 800); 
        scene.getStylesheets().add(cssPath);

        stage.setTitle("Countbelly Dashboard");
        stage.setScene(scene);
        
        stage.setFullScreen(false); 
        stage.setMaximized(true); 
        
        refreshDashboardContent(); 
        
        stage.show();
    }
    
    /**
     * Método que refaz a busca de dados e reconstrói o conteúdo do dashboard.
     */
    private void refreshDashboardContent() {
        // 1. Obter todos os dados reais do Supabase.
        DashboardData data = service.fetchDashboardData();
        List<DashboardService.AgendaItem> fullAgenda = service.fetchAgendaDetails(); 
        
        // 2. Separa e LIMTA a Agenda
        LocalDateTime now = LocalDateTime.now();
        
        // Limita a 3 próximas (ordenadas da mais próxima para a mais distante)
        List<DashboardService.AgendaItem> futureAgenda = fullAgenda.stream()
            .filter(item -> LocalDateTime.parse(item.dataHora, formatter).isAfter(now))
            .sorted(Comparator.comparing(item -> LocalDateTime.parse(item.dataHora, formatter))) 
            .limit(3) 
            .collect(Collectors.toList());
            
        // Limita as 3 últimas (ordenadas da mais recente para a mais antiga)
        List<DashboardService.AgendaItem> pastAgenda = fullAgenda.stream()
            .filter(item -> !LocalDateTime.parse(item.dataHora, formatter).isAfter(now)) 
            .sorted(Comparator.comparing((DashboardService.AgendaItem item) -> LocalDateTime.parse(item.dataHora, formatter)).reversed())
            .limit(3) 
            .collect(Collectors.toList());


        // 3. HEADER: Título, Status, Refresh, Zoom e Logout (TOP)
        
        // Cria os botões de Ação
        Button refreshButton = new Button("🔄 Atualizar Dados");
        refreshButton.getStyleClass().addAll("action-button", "refresh-button");
        refreshButton.setOnAction(e -> refreshDashboardContent()); 
        
        Button zoomInButton = new Button("Zoom +");
        zoomInButton.getStyleClass().addAll("action-button", "zoom-button");
        zoomInButton.setOnAction(e -> zoomIn());

        Button zoomOutButton = new Button("Zoom -");
        zoomOutButton.getStyleClass().addAll("action-button", "zoom-button");
        zoomOutButton.setOnAction(e -> zoomOut());

        // Botão de Logout
        Button logoutButton = new Button("Sair 🚪");
        logoutButton.getStyleClass().addAll("action-button", "logout-button"); 
        logoutButton.setOnAction(e -> showLoginScreen(primaryStage)); 
        
        // Controles de Zoom agrupados
        HBox zoomControls = new HBox(10); 
        zoomControls.getChildren().addAll(zoomOutButton, zoomInButton);
        zoomControls.setAlignment(Pos.CENTER); // Centraliza verticalmente o grupo Zoom
        
        // Combina todos os botões de ação em um único HBox
        HBox actionControlsRight = new HBox(20); // Espaçamento entre os grupos de botões
        actionControlsRight.setAlignment(Pos.CENTER); // Alinhamento CENTER para centralizar verticalmente todos os botões
        actionControlsRight.getChildren().addAll(refreshButton, zoomControls, logoutButton); 

        // Título e Status (Esquerda)
        Label titleLabel = createTitleLabel("📊 Dashboard - Countbelly Chatbot");
        
        VBox titleAndStatus = new VBox(5);
        titleAndStatus.getChildren().addAll(
            titleLabel,
            createStatusLabel("Status da Fonte: " + data.getStatusConexao())
        );
        titleAndStatus.setAlignment(Pos.CENTER_LEFT); 

        // COMBINAÇÃO NO TOPO: Título (Esquerda) e Ações (Direita)
        HBox topHeader = new HBox(30);
        topHeader.setAlignment(Pos.CENTER_LEFT); 
        
        topHeader.getChildren().add(titleAndStatus);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topHeader.getChildren().add(spacer);
        
        topHeader.getChildren().add(actionControlsRight); 
        
        rootDashboard.setTop(topHeader);
        
        // ------------------------------------------------------------------
        // --- LAYOUT CENTRAL COM ALINHAMENTO CORRIGIDO DA AGENDA ---
        // ------------------------------------------------------------------
        
        // CORREÇÃO: Variável deve ser declarada aqui dentro do método
        VBox centralContentVBox = new VBox(25); 
        VBox.setVgrow(centralContentVBox, Priority.ALWAYS); // Permite o crescimento vertical total

        // 1. LINHA DO TOPO: Cartões de Métrica (60%) + Agenda Futura (40%)
        
        HBox topRowLayout = new HBox(30);
        
        // 1A. Cartões de Métrica
        HBox topMetrics = createTopMetrics(data); 
        
        // 1B. Agenda Futura
        VBox futureAgendaPanel = createAgendaDetailPanel(futureAgenda, "Próximas Reuniões Agendadas");
        VBox.setVgrow(futureAgendaPanel, Priority.NEVER); 
        
        topRowLayout.getChildren().addAll(topMetrics, futureAgendaPanel);
        
        HBox.setHgrow(topMetrics, Priority.ALWAYS);
        HBox.setHgrow(futureAgendaPanel, Priority.ALWAYS);
        
        VBox.setVgrow(topRowLayout, Priority.NEVER);
        centralContentVBox.getChildren().add(topRowLayout);


        // 2. GRID PRINCIPAL (Gráficos vs Agenda Passada)
        
        GridPane gridChartsAndAgenda = new GridPane();
        gridChartsAndAgenda.setHgap(30);
        gridChartsAndAgenda.setVgap(25);
        VBox.setVgrow(gridChartsAndAgenda, Priority.ALWAYS); 
        
        // Restrições de Coluna: 60% (Gráficos) e 40% (Agenda Passada)
        ColumnConstraints gridCol1 = new ColumnConstraints(); 
        gridCol1.setHgrow(Priority.ALWAYS); 
        gridCol1.setPercentWidth(60); 
        
        ColumnConstraints gridCol2 = new ColumnConstraints(); 
        gridCol2.setHgrow(Priority.ALWAYS);
        gridCol2.setPercentWidth(40); 
        gridChartsAndAgenda.getColumnConstraints().addAll(gridCol1, gridCol2); 
        
        // Restrição de Linha: Permite o crescimento vertical
        RowConstraints gridRow1 = new RowConstraints();
        gridRow1.setVgrow(Priority.ALWAYS); 
        gridChartsAndAgenda.getRowConstraints().add(gridRow1);


        // 2a. Painel de Gráficos (Coluna 0: 60%)
        VBox chartVBoxStack = new VBox(25);
        VBox.setVgrow(chartVBoxStack, Priority.ALWAYS); 
        
        PieChart pieChart = createSegmentationChart(data.getClientesPF(), data.getClientesPJ());
        BarChart<String, Number> barChart = createDailyVolumeChart(service.fetchDailyMessageVolume());

        pieChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        barChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        VBox.setVgrow(pieChart, Priority.ALWAYS);
        VBox.setVgrow(barChart, Priority.ALWAYS);
        
        chartVBoxStack.getChildren().addAll(pieChart, barChart);
        GridPane.setValignment(chartVBoxStack, VPos.TOP); 
        gridChartsAndAgenda.add(chartVBoxStack, 0, 0);


        // 2b. Painel de Agenda Passada (Coluna 1: 40%)
        VBox pastAgendaPanel = createAgendaDetailPanel(pastAgenda, "Histórico de Reuniões (Passadas)");
        VBox.setVgrow(pastAgendaPanel, Priority.ALWAYS); 
        GridPane.setValignment(pastAgendaPanel, VPos.TOP); 
        gridChartsAndAgenda.add(pastAgendaPanel, 1, 0);


        // Adiciona o Grid ao VBox Central
        centralContentVBox.getChildren().add(gridChartsAndAgenda);
        
        // APLICA O ZOOM ao VBox Central
        centralContentVBox.setScaleX(zoomScale);
        centralContentVBox.setScaleY(zoomScale);
        
        rootDashboard.setCenter(centralContentVBox); 
        BorderPane.setMargin(centralContentVBox, new Insets(20, 0, 0, 0)); 
    }
    
    // --- MÉTODOS DE ZOOM (Mantidos) ---
    
    private void zoomIn() {
        if (zoomScale < MAX_ZOOM) {
            zoomScale = Math.min(zoomScale + ZOOM_STEP, MAX_ZOOM);
            applyZoom();
        }
    }
    
    private void zoomOut() {
        if (zoomScale > MIN_ZOOM) {
            zoomScale = Math.max(zoomScale - ZOOM_STEP, MIN_ZOOM);
            applyZoom();
        }
    }

    private void applyZoom() {
        if (rootDashboard != null) {
            refreshDashboardContent();
        }
    }
    
    // --- MÉTODOS AUXILIARES ---

    private VBox createAgendaDetailPanel(List<DashboardService.AgendaItem> agenda, String panelTitle) {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("detail-panel");
        
        Label title = new Label(panelTitle);
        title.getStyleClass().add("detail-title");
        panel.getChildren().add(title);

        if (agenda.isEmpty()) {
            Label noData = new Label("Nenhuma reunião encontrada.");
            noData.getStyleClass().add("agenda-item");
            panel.getChildren().add(noData);
        } else {
            // Usa um VBox interno para os itens da agenda, permitindo ScrollPane se necessário em outras versões
            VBox agendaItemsBox = new VBox(5);
            for (DashboardService.AgendaItem item : agenda) {
                Label detail = new Label(
                    String.format("Cliente: %s (%s)\nData/Hora: %s", 
                        item.nomeCliente, item.clienteType, item.dataHora)
                );
                detail.getStyleClass().add("agenda-item");
                agendaItemsBox.getChildren().add(detail);
            }
            panel.getChildren().add(agendaItemsBox);
        }
        
        // Garante que o painel de agenda se expanda
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setMaxHeight(Double.MAX_VALUE); 
        
        return panel;
    }

    private HBox createTopMetrics(DashboardData data) {
        HBox hbox = new HBox(20);
        hbox.getChildren().addAll(
            createMetricCard("Total de Sessões", String.valueOf(data.getClientesAtendidosTotal()), "metric-value"),
            createMetricCard("Em Atendimento (Bot)", String.valueOf(data.getClientesAguardando()), "metric-value"), 
            createMetricCard("Atend. Humano", String.valueOf(data.getAtendimentoHumano()), "metric-danger"),
            createMetricCard("Reuniões Agendadas", String.valueOf(data.getReunioesAgendadas()), "metric-value")
        );
        HBox.setHgrow(hbox, Priority.ALWAYS); 
        return hbox;
    }

    private VBox createMetricCard(String title, String value, String valueStyleClass) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().addAll("metric-value", valueStyleClass);

        VBox card = new VBox(5);
        card.getStyleClass().add("metric-card");
        card.getChildren().addAll(titleLabel, valueLabel);
        
        VBox.setVgrow(card, Priority.NEVER); 
        card.setMaxHeight(Region.USE_PREF_SIZE); 
        HBox.setHgrow(card, Priority.ALWAYS); 
        
        return card;
    }

    private PieChart createSegmentationChart(int pfCount, int pjCount) {
        ObservableList<Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Clientes Pessoa Física (PF) (" + pfCount + ")", pfCount), 
            new PieChart.Data("Clientes Pessoa Jurídica (PJ) (" + pjCount + ")", pjCount) 
        );
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Segmentação PF vs PJ");
        chart.setLegendSide(javafx.geometry.Side.BOTTOM);
        return chart;
    }
    
    private BarChart<String, Number> createDailyVolumeChart(Map<String, Integer> volumeData) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        
        chart.setTitle("Mensagens Recebidas por Hora (Hoje)");
        xAxis.setLabel("Hora do Dia");
        yAxis.setLabel("Volume de Mensagens");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Mensagens");
        
        for (Map.Entry<String, Integer> entry : volumeData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().add(series);
        chart.setLegendVisible(false);
        return chart;
    }

    private Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("titulo-principal");
        return label;
    }

    private Label createStatusLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("metric-title");
        return label;
    }

    public static void main(String[] args) {
        launch(args);
    }
}