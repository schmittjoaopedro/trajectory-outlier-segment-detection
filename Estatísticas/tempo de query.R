library(ggplot2)
data <- read.csv("statistics_bra.csv")
#data <- read.csv("statistics_eua.csv")
data <- data[data$R1 != data$R2,]

temp <- data.frame(y = data$QT, x = data$PTQTDALL)
temp$y <- temp$y / 10^9
temp <- temp[order(temp$x),]
options(scipen=5)
plot(temp$x, temp$y, pch = 19, col = "lightgray", xlab = "Número de pontos", 
     ylab = "Tempo da consulta (s)")
title(main = "Tempo de consulta da base - Joinville (Brasil)")
glm1 = glm(temp$y ~ temp$x, family = "poisson")
lines(temp$x, glm1$fitted.values, col = "black", lwd = 3)
