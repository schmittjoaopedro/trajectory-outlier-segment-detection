library(ggplot2)
#data <- read.csv("statistics_bra.csv")
data <- read.csv("statistics_eua.csv")
data <- data[data$R1 != data$R2,]

#PT x STD
temp <- data.frame(y = data$PT, t = data$PTQTDEPR)
temp$y <- temp$y / 10^9
temp <- temp[order(temp$t),]
plot(temp$t, temp$y, pch = 19, col = "cadetblue1", xlab = "Número médio de pontos", ylab = "Tempo médio (s)")
glm1 = glm(temp$y ~ temp$t, family = "poisson")
lines(temp$t, glm1$fitted.values, col = "blue", lwd = 3)
#title(main = "Tempo médio da execução (TODS) - Joinville (Brasil)")
title(main = "Tempo médio da execução (TODS) - San Francisco (EUA)")
