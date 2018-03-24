#jpeg(file="all_plots.jpeg", width = 8, height = 8, units = "in", res = 400)

dataBR <- read.csv("statistics_bra.csv")
dataUS <- read.csv("statistics_eua.csv")
dataBR <- dataBR[dataBR$R1 != dataBR$R2,]
dataUS <- dataUS[dataUS$R1 != dataUS$R2,]

par(mfrow = c(3,2))

#Consulta base BRASIL
temp <- data.frame(y = dataBR$QT, x = dataBR$PTQTDALL)
temp$y <- temp$y / 10^9
temp <- temp[order(temp$x),]
options(scipen=5)
plot(temp$x, temp$y, pch = 19, col = "lightgray", xlab = "Número de pontos", 
     ylab = "Tempo da consulta (s)")
title(main = "Tempo de consulta da base - Joinville (Brasil)")
glm1 = glm(temp$y ~ temp$x, family = "poisson")
lines(temp$x, glm1$fitted.values, col = "black", lwd = 3)

#Consulta base EUA
temp <- data.frame(y = dataUS$QT, x = dataUS$PTQTDALL)
temp$y <- temp$y / 10^9
temp <- temp[order(temp$x),]
options(scipen=5)
plot(temp$x, temp$y, pch = 19, col = "lightgray", xlab = "Número de pontos", 
     ylab = "Tempo da consulta (s)")
title(main = "Tempo de consulta da base - San Francisco (EUA)")
glm1 = glm(temp$y ~ temp$x, family = "poisson")
lines(temp$x, glm1$fitted.values, col = "black", lwd = 3)

#Tempo execução por trajetória Brasil
temp <- data.frame(y = dataBR$PT, x = dataBR$STD)
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
temp <- data.frame(y = dataBR$PT, x = dataBR$NSTD)
temp$y <- temp$y / 10^9
temp2 <- data.frame()
for(g in unique(temp$x)) {
    temp2 <- rbind(temp2, data.frame(x = g, y = mean(temp$y[temp$x == g])))
}
temp2 <- temp2[order(temp2$x),]
points(temp2$x, temp2$y, col = "coral", pch = 19)
glm1 = glm(temp2$y ~ temp2$x, family = "poisson")
lines(temp2$x, glm1$fitted.values, col = "red", lwd = 3)
title(main = "Tempo por trajetória - Joinville (Brasil)")
legend(x = "topright", legend = c("Alternativas","Padrões"), col = c("coral", "cadetblue1"), 
       lwd = 5, pch = c(19,19), lty = c(0,0))


#Tempo execução por trajetória EUA
temp <- data.frame(y = dataUS$PT, x = dataUS$STD)
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
temp <- data.frame(y = dataUS$PT, x = dataUS$NSTD)
temp$y <- temp$y / 10^9
temp2 <- data.frame()
for(g in unique(temp$x)) {
    temp2 <- rbind(temp2, data.frame(x = g, y = mean(temp$y[temp$x == g])))
}
temp2 <- temp2[order(temp2$x),]
points(temp2$x, temp2$y, col = "coral", pch = 19)
glm1 = glm(temp2$y ~ temp2$x, family = "poisson")
lines(temp2$x, glm1$fitted.values, col = "red", lwd = 3)
title(main = "Tempo por trajetória - San Francisco (EUA)")
legend(x = "topright", legend = c("Alternativas","Padrões"), col = c("coral", "cadetblue1"), 
       lwd = 5, pch = c(19,19), lty = c(0,0))

#Tempo execução por pontos (Brasil)
temp <- data.frame(y = dataBR$PT, t = dataBR$PTQTDEPR)
temp$y <- temp$y / 10^9
temp <- temp[order(temp$t),]
plot(temp$t, temp$y, pch = 19, col = "cadetblue1", xlab = "Número médio de pontos", ylab = "Tempo médio (s)")
glm1 = glm(temp$y ~ temp$t, family = "poisson")
lines(temp$t, glm1$fitted.values, col = "blue", lwd = 3)
title(main = "Tempo por pontos - Joinville (Brasil)")


#Tempo execução por pontos (EUA)
temp <- data.frame(y = dataUS$PT, t = dataUS$PTQTDEPR)
temp$y <- temp$y / 10^9
temp <- temp[order(temp$t),]
plot(temp$t, temp$y, pch = 19, col = "cadetblue1", xlab = "Número médio de pontos", ylab = "Tempo médio (s)")
glm1 = glm(temp$y ~ temp$t, family = "poisson")
lines(temp$t, glm1$fitted.values, col = "blue", lwd = 3)
title(main = "Tempo por pontos - San Francisco (USA)")

#dev.off()