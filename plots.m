%% linear regression
clf
x = [1 1.5 3 7 5 6 7 8 9];
y = [1 2 3.5 5 8 6 5.9 8.3 9.2];
x1 = linspace(0,10,1000);
y1 = x1;

hold on
fig = plot(y1,x1);

scatter(x,y,"filled")

xlabel("Input variable (x)")
ylabel("Output variable (y)")

saveas(fig, 'linear_reg','epsc')

%% Latency vs window size
clf
latency = [1.4 3.4 7.2 8 9.1 10.5 11.3];
wndw_size = [1 2 3 4 5 6 7];

fig = scatter(wndw_size, latency,"filled", "red")

xlabel("Window size", "FontSize", 15);
ylabel("Latency [seconds]", "FontSize", 15)
xlim([0 8])
ylim([0 12])

saveas(fig, 'lat_vs_wndw', 'epsc')


%% avg. cluster size vs window size
clf
avg_cluster = [1 2 3 4 4 4 4]
wndw_size = [1 2 3 4 5 6 7]

fig = scatter(wndw_size,avg_cluster,"filled", "red")

xlabel("Window size", "FontSize", 15)
ylabel("Average cluster height [n. of layers]", "FontSize", 15)
xlim([0 8])
ylim([0 5])

saveas(fig, 'IMG/avg_cluster_vs_wndw', 'epsc')



%% gen random
x = rand(1,20) + 1;
y = rand(1,20) + 1;


%% plot for presentation
clf
hold on

z = zeros(1,20);

scatter3(x,y,z,"filled")

xPlane = linspace(0,10,10);
yPlane = linspace(0,10,10);

[X, Y] = meshgrid(xPlane, yPlane);

Z = zeros(size(X))

s = surf(X,Y,Z);
alpha(s, 0.5)
grid off
view(2)


xlim([0,10])
ylim([0,10])
zlim([-1, 2])

saveas(s, "IMG/image2d", "svg")

%% image3d

view(3)
grid on

saveas(s, "IMG/image3d", "svg")

%% second plane

z1 = ones(1,20)

scatter3(x,y,z1,"filled")

xPlane = linspace(0,10,10);
yPlane = linspace(0,10,10);

[X, Y] = meshgrid(xPlane, yPlane);

Z = ones(size(X))

s = surf(X,Y,Z);
alpha(s, 0.5)

saveas(s, "IMG/2planes3d", "epsc")

%% line between two points

p = plot3([x(1), x(3)],[y(1), y(3)], [0, 1], 'linewidth', 3)

saveas(s, "IMG/planeswithline", "epsc")

%% plot of geometric summary

ellipsoid(1.5, 1.5, 0.5, 0.8, 0.8, 1)
xlim([0 10])
ylim([0 10])

saveas(s, "IMG/geo_summary", "epsc")


%% plot of cluster_size vs epsilon
clf
x = [1 2 3 4 5 6 7];
y = [1 2 3 3 4 4.3 4.4];

hold on

scatter(x,y,"filled")

xlabel("Epsilon")
ylabel("Cluster Size")
xlim([0 8])
ylim([0 6])

saveas(gcf, "IMG/cluster_size_vs_eps", "png")

%% box plot similaity
clf



row_10 = 1;
row_5 = 197;
row_15 = 50
row_20 = 99

column = 6

M_neigh_size_5 = (csvread('output_object_1.csv',row_5,column,[row_5,column,row_5+20,column])./405).*100
M_neigh_size_10 = (csvread('output_object_1.csv',row_10,column,[row_10,column,row_10+20,column])./405).*100
M_neigh_size_15 = (csvread('output_object_1.csv',row_15,column,[row_15,column,row_15+20,column])./405).*100
M_neigh_size_20 = (csvread('output_object_1.csv',row_20,column,[row_20,column,row_20+20,column])./405).*100


boxplot([M_neigh_size_5, M_neigh_size_10, M_neigh_size_15, M_neigh_size_20], 'Labels', {'5', '10', '15', '20'})

xlabel('Neighborhood size')
ylabel('Similarity[%]')

saveas(gcf, "IMG/boxplot_similarity", "png")

%% latency

clf
row_10 = 1;
row_5 = 197;
row_15 = 50
row_20 = 99

column = 7

M_neigh_size_5 = csvread('output_object_1.csv',row_5,column,[row_5,column,row_5+20,column])
M_neigh_size_10 = csvread('output_object_1.csv',row_10,column,[row_10,column,row_10+20,column])
M_neigh_size_15 = csvread('output_object_1.csv',row_15,column,[row_15,column,row_15+20,column])
M_neigh_size_20 = csvread('output_object_1.csv',row_20,column,[row_20,column,row_20+20,column])


boxplot([M_neigh_size_5, M_neigh_size_10, M_neigh_size_15, M_neigh_size_20], 'Labels', {'5', '10', '15', '20'})
ylabel('Latency[ms]')
xlabel('Neighborhood size')

saveas(gcf, "IMG/boxplot_latency", "png")

%% plot of hroughput
clf
y = [2 2.4 1.7 1];
x = [5 10 15 20];

hold on

scatter(x,y,"filled")

xlabel("Neighborhood size")
ylabel("Throughput")
xlim([0 25])
ylim([0 6])

saveas(gcf, "IMG/throughput", "png")
