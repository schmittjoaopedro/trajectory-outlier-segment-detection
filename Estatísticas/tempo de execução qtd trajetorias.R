library(ggplot2)
#data <- read.csv("statistics_bra.csv")
data <- read.csv("statistics_eua.csv")
data <- data[data$R1 != data$R2,]

#PT x STD
temp <- data.frame(y = data$PT, x = data$STD)
temp$y <- temp$y / 10^9
temp2 <- data.frame()
for(g in unique(temp$x)) {
    temp2 <- rbind(temp2, data.frame(x = g, y = mean(temp$y[temp$x == g])))
}
temp2 <- temp2[order(temp2$x),]
plot(temp2$x, temp2$y, pch = 19, col = "cadetblue1", xlab = "Número de trajetórias", 
     ylab = "Tempo médio (s)", ylim = c(0,0.1))
glm1 = glm(temp2$y ~ temp2$x, family = "poisson")
lines(temp2$x, glm1$fitted.values, col = "blue", lwd = 3)

#PT x NSTD
temp <- data.frame(y = data$PT, x = data$NSTD)
temp$y <- temp$y / 10^9
temp2 <- data.frame()
for(g in unique(temp$x)) {
    temp2 <- rbind(temp2, data.frame(x = g, y = mean(temp$y[temp$x == g])))
}
temp2 <- temp2[order(temp2$x),]
points(temp2$x, temp2$y, col = "coral", pch = 19)
glm1 = glm(temp2$y ~ temp2$x, family = "poisson")
lines(temp2$x, glm1$fitted.values, col = "red", lwd = 3)
title(main = "Tempo médio de execução (TODS) - San Francisco (EUA)")
#title(main = "Tempo médio de execução (TODS) - Joinville (Brasil)")
legend(x = "topright", legend = c("Alternativas","Padrões"), col = c("coral", "cadetblue1"), 
       lwd = 5, pch = c(19,19), lty = c(0,0))