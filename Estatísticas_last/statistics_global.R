library(dplyr)

setwd("/home/joao/Área de Trabalho/Mestrado/GIT/trajectory-outlier-segment-detection/Estatísticas_last")
data <- read.csv("statistics_bra_final.csv")
data <- rbind(data, read.csv("statistics_eua_final.csv"));

data$TRAJ <- data$STD + data$NSTD
data$TOTT <- data$CT + data$GT + data$STDT + data$SEGT

par(mfrow = c(1,2), mar = c(5,5,2,2))
if(tPlot) {
    temp <- data.frame(y = data$TOTT, x = data$PTQTDEPR)
    temp$y <- temp$y / 10^9
    temp <- temp[order(temp$x),]
    options(scipen=5)
    temp$x <- as.integer(temp$x / 10000)
    temp$x <- temp$x * 10000
    temp$lbl = "empty"
    temp[temp$x == 0,]$lbl <- "0-10"
    temp[temp$x == 10000,]$lbl <- "10-20"
    temp[temp$x == 20000,]$lbl <- "20-30"
    temp[temp$x == 30000,]$lbl <- "30-40"
    temp[temp$x == 40000,]$lbl <- "40-50"
    temp[temp$x == 50000,]$lbl <- "50-60"
    temp[temp$x == 60000,]$lbl <- "60-70"
    #temp <- temp %>% group_by(x) %>% summarise(y = mean(y)) %>% as.data.frame()
    #temp <- temp[temp$y < 20,]
    #plot(temp$x, temp$y)
    #glm1 = glm(temp$y ~ temp$x, family = "poisson")
    #lines(temp$x, glm1$fitted.values, col = "black", lwd = 3)
    boxplot(y ~ lbl, data = temp, xlab = "Geographical coordinates (thousands)", ylab = "Time (seconds)", cex.lab=2, cex.axis=2)
    title("B) Average time by number of coordinates", cex.main = 2)
}

if(bpPlot == TRUE) {
    temp <- data.frame(y = data$TOTT, x = data$PTQTDEPR)
    temp <- temp[order(temp$x),]
    options(scipen=5)
    temp$x <- as.integer(temp$x / 10000)
    temp$x <- temp$x * 10000
    temp <- temp %>% group_by(x) %>% summarise(y = n()) %>% as.data.frame()
    bp <- barplot(temp$y, names.arg = c("0-10","10-20","20-30","30-40","40-50","50-60","60-70"),
            xlab = "Geographical coordinates (thousands)", ylab = "Number of samples", ylim = c(0,5000), cex.lab=2, cex.axis=2, cex.names = 2)
    title("A) Number of analyzes vs number of trajectories", cex.main = 2)
    text(x = bp, y = temp$y, label = temp$y, pos = 3, cex = 2, col = "black")
}
if(bpPlot2 == TRUE) {
    temp <- data.frame(x = data$PTQTDEPR, y1 = data$CT, y2 = data$GT, y3 = data$STDT, y4 = data$SEGT)
    temp$y1 <- temp$y1 / 10^9
    temp$y2 <- temp$y2 / 10^9
    temp$y3 <- temp$y3 / 10^9
    temp$y4 <- temp$y4 / 10^9
    temp$x <- as.integer(temp$x / 10000)
    temp$x <- temp$x * 10000
    temp$lbl = "empty"
    temp[temp$x == 0,]$lbl <- "0-10"
    temp[temp$x == 10000,]$lbl <- "10-20"
    temp[temp$x == 20000,]$lbl <- "20-30"
    temp[temp$x == 30000,]$lbl <- "30-40"
    temp[temp$x == 40000,]$lbl <- "40-50"
    temp[temp$x == 50000,]$lbl <- "50-60"
    temp[temp$x == 60000,]$lbl <- "60-70"
    temp <- temp %>% group_by(x) %>% summarise(y1 = mean(y1), y2 = mean(y2), y3 = mean(y3), y4 = mean(y4)) %>% as.data.frame()
    
    par(mfrow = c(2,2), mar = c(5,5,2,2))
    vfs = 2
    
    bp <- barplot(temp$y1, names.arg = c("0-10","10-20","20-30","30-40","40-50","50-60","60-70"),
            xlab = "Geographical coordinates (thousands)", ylab = "Time (seconds)", ylim = c(0,15), cex.lab=vfs, cex.axis=vfs, cex.names = vfs)
    title("Step 1 (Recovery)", cex.main = vfs)
    text(x = bp, y = temp$y1, label = round(temp$y1, 3), pos = 3, cex = vfs, col = "black")
    
    bp <- barplot(temp$y2, names.arg = c("0-10","10-20","20-30","30-40","40-50","50-60","60-70"),
                  xlab = "Geographical coordinates (thousands)", ylab = "Time (seconds)", ylim = c(0,0.01), cex.lab=vfs, cex.axis=vfs, cex.names = vfs)
    title("Step 2 (Grouping)", cex.main = vfs)
    text(x = bp, y = temp$y2, label = round(temp$y2, 3), pos = 3, cex = vfs, col = "black")
    
    bp <- barplot(temp$y3, names.arg = c("0-10","10-20","20-30","30-40","40-50","50-60","60-70"),
                  xlab = "Geographical coordinates (thousands)", ylab = "Time (seconds)", ylim = c(0,0.02), cex.lab=vfs, cex.axis=vfs, cex.names = vfs)
    title("Step 3 (Selection)", cex.main = vfs)
    text(x = bp, y = temp$y3, label = round(temp$y3, 3), pos = 3, cex = vfs, col = "black")
    
    bp <- barplot(temp$y4, names.arg = c("0-10","10-20","20-30","30-40","40-50","50-60","60-70"),
                  xlab = "Geographical coordinates (thousands)", ylab = "Time (seconds)", ylim = c(0,1), cex.lab=vfs, cex.axis=vfs, cex.names = vfs)
    title("Step 4 (Segmentation)", cex.main = vfs)
    text(x = bp, y = temp$y4, label = round(temp$y4, 3), pos = 3, cex = vfs, col = "black")
    
}