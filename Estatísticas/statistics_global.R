library(dplyr)

setwd("/home/joao/Área de Trabalho/Mestrado/Estatísticas")

#setwd("/home/joao/Área de Trabalho/Mestrado/output/TODSv2/Joinville")
#data <- read.csv("statistics_bra_final.csv")
#data <- rbind(data, read.csv("statistics_bra3.csv"))
#data <- rbind(data, read.csv("statistics_bra4.csv"))

data <- read.csv("statistics_bra_final.csv")
data <- rbind(data, read.csv("statistics_eua_final.csv"));

data$TRAJ <- data$STD + data$NSTD
data$TOTT <- data$CT + data$GT + data$STDT + data$SEGT

temp <- data.frame(y = data$TOTT, x = data$PTQTDEPR)
#temp <- data.frame(y = data$TOTT, x = data$PTQTDALL)
#temp <- data.frame(y = data$QT, x = data$PTQTDALL)
#temp <- data.frame(y = data$CT, x = data$PTQTDALL)
#temp <- data.frame(y = data$GT, x = data$PTQTDALL)
#temp <- data.frame(y = data$STDT, x = data$PTQTDALL)
#temp <- data.frame(y = data$SEGT, x = data$PTQTDALL)
temp$y <- temp$y / 10^9
temp <- temp[order(temp$x),]
options(scipen=5)

temp$x <- as.integer(temp$x / 1000)
temp$x <- temp$x * 1000
temp <- temp %>% group_by(x) %>% summarise(y = mean(y)) %>% as.data.frame()
#temp <- temp[temp$y < 20,]

plot(temp$x, temp$y)
glm1 = glm(temp$y ~ temp$x, family = "poisson")
lines(temp$x, glm1$fitted.values, col = "black", lwd = 3)

#boxplot(y ~ x, data = temp)

if(bpPlot == true) {
    temp <- data.frame(y = data$TOTT, x = data$PTQTDEPR)
    temp <- temp[order(temp$x),]
    options(scipen=5)
    temp$x <- as.integer(temp$x / 5000)
    temp$x <- temp$x * 5000
    temp <- temp %>% group_by(x) %>% summarise(y = n()) %>% as.data.frame()
    barplot(temp$y)  
}